/*
* AMRIT – Accessible Medical Records via Integrated Technology 
* Integrated EHR (Electronic Health Records) Solution 
*
* Copyright (C) "Piramal Swasthya Management and Research Institute" 
*
* This file is part of AMRIT.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see https://www.gnu.org/licenses/.
*/
package com.iemr.mmu.service.health;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;

class HealthServiceTest {

	/** A data source whose health query returns a row, and whose diagnostic queries are quiet. */
	private DataSource healthyDataSource(int lockWaits, int slowQueries) throws SQLException {
		DataSource dataSource = mock(DataSource.class);
		when(dataSource.getConnection()).thenAnswer(invocation -> connection(lockWaits, slowQueries));
		return dataSource;
	}

	private Connection connection(int lockWaits, int slowQueries) throws SQLException {
		Connection connection = mock(Connection.class);
		when(connection.prepareStatement(anyString())).thenAnswer(invocation -> {
			String sql = invocation.getArgument(0);
			if (sql.contains("health_check")) {
				return statementReturning(1);
			}
			if (sql.contains("metadata lock")) {
				return statementReturning(lockWaits);
			}
			return statementReturning(slowQueries);
		});
		return connection;
	}

	private PreparedStatement statementReturning(int count) throws SQLException {
		PreparedStatement statement = mock(PreparedStatement.class);
		ResultSet resultSet = mock(ResultSet.class);
		when(resultSet.next()).thenReturn(true);
		when(resultSet.getInt(1)).thenReturn(count);
		when(statement.executeQuery()).thenReturn(resultSet);
		return statement;
	}

	@SuppressWarnings("unchecked")
	private RedisTemplate<String, Object> redisAnswering(String pong) {
		RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
		when(redisTemplate.execute(any(RedisCallback.class))).thenAnswer(invocation -> {
			RedisConnection connection = mock(RedisConnection.class);
			when(connection.ping()).thenReturn(pong);
			return ((RedisCallback<String>) invocation.getArgument(0)).doInRedis(connection);
		});
		return redisTemplate;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> componentOf(Map<String, Object> health, String name) {
		return ((Map<String, Map<String, Object>>) health.get("components")).get(name);
	}

	@Test
	@DisplayName("both components report UP when the database and Redis answer")
	void checkHealth_reportsEverythingUpWhenBothComponentsAnswer() throws Exception {
		HealthService service = new HealthService(healthyDataSource(0, 0), redisAnswering("PONG"));
		try {
			Map<String, Object> health = service.checkHealth();

			assertEquals("UP", health.get("status"));
			assertEquals("UP", componentOf(health, "mysql").get("status"));
			assertEquals("UP", componentOf(health, "redis").get("status"));
			assertNotNull(health.get("timestamp"));
		} finally {
			service.shutdown();
		}
	}

	@Test
	@DisplayName("MySQL reports DOWN when the connection cannot be opened")
	void checkHealth_reportsMysqlDownWhenTheConnectionFails() throws Exception {
		DataSource dataSource = mock(DataSource.class);
		when(dataSource.getConnection()).thenThrow(new SQLException("connection refused"));

		HealthService service = new HealthService(dataSource, redisAnswering("PONG"));
		try {
			Map<String, Object> health = service.checkHealth();

			assertEquals("DOWN", health.get("status"));
			assertEquals("DOWN", componentOf(health, "mysql").get("status"));
			assertEquals("CRITICAL", componentOf(health, "mysql").get("severity"));
			assertEquals("MySQL connection failed", componentOf(health, "mysql").get("error"));
		} finally {
			service.shutdown();
		}
	}

	@Test
	@DisplayName("MySQL reports DOWN when the health query returns no row")
	void checkHealth_reportsMysqlDownWhenTheHealthQueryReturnsNothing() throws Exception {
		Connection connection = mock(Connection.class);
		PreparedStatement statement = mock(PreparedStatement.class);
		ResultSet resultSet = mock(ResultSet.class);
		when(resultSet.next()).thenReturn(false);
		when(statement.executeQuery()).thenReturn(resultSet);
		when(connection.prepareStatement(anyString())).thenReturn(statement);
		DataSource dataSource = mock(DataSource.class);
		when(dataSource.getConnection()).thenReturn(connection);

		HealthService service = new HealthService(dataSource, redisAnswering("PONG"));
		try {
			Map<String, Object> health = service.checkHealth();

			assertEquals("No result from health check query", componentOf(health, "mysql").get("error"));
		} finally {
			service.shutdown();
		}
	}

	@Test
	@DisplayName("MySQL reports DEGRADED when the database is waiting on locks")
	void checkHealth_reportsMysqlDegradedWhenTheDatabaseIsWaitingOnLocks() throws Exception {
		HealthService service = new HealthService(healthyDataSource(2, 0), redisAnswering("PONG"));
		try {
			Map<String, Object> health = service.checkHealth();

			assertEquals("DEGRADED", health.get("status"));
			assertEquals("DEGRADED", componentOf(health, "mysql").get("status"));
			assertEquals("WARNING", componentOf(health, "mysql").get("severity"));
		} finally {
			service.shutdown();
		}
	}

	@Test
	@DisplayName("MySQL reports DEGRADED when too many slow queries are running")
	void checkHealth_reportsMysqlDegradedWhenTooManySlowQueriesAreRunning() throws Exception {
		HealthService service = new HealthService(healthyDataSource(0, 5), redisAnswering("PONG"));
		try {
			assertEquals("DEGRADED", service.checkHealth().get("status"));
		} finally {
			service.shutdown();
		}
	}

	@Test
	@DisplayName("the advanced diagnostics are only re-run once the throttle window passes")
	void checkHealth_reusesTheCachedDiagnosticsWithinTheThrottleWindow() throws Exception {
		HealthService service = new HealthService(healthyDataSource(2, 0), redisAnswering("PONG"));
		try {
			service.checkHealth();

			assertEquals("DEGRADED", service.checkHealth().get("status"));
		} finally {
			service.shutdown();
		}
	}

	@Test
	@DisplayName("Redis reports DOWN when the server does not answer PING")
	void checkHealth_reportsRedisDownWhenPingIsNotAnswered() throws Exception {
		HealthService service = new HealthService(healthyDataSource(0, 0), redisAnswering("NOPE"));
		try {
			Map<String, Object> health = service.checkHealth();

			assertEquals("DOWN", health.get("status"));
			assertEquals("Redis PING failed", componentOf(health, "redis").get("error"));
		} finally {
			service.shutdown();
		}
	}

	@Test
	@DisplayName("Redis reports DOWN when the connection fails")
	@SuppressWarnings("unchecked")
	void checkHealth_reportsRedisDownWhenTheConnectionFails() throws Exception {
		RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
		when(redisTemplate.execute(any(RedisCallback.class))).thenThrow(new RuntimeException("connection refused"));

		HealthService service = new HealthService(healthyDataSource(0, 0), redisTemplate);
		try {
			assertEquals("Redis connection failed", componentOf(service.checkHealth(), "redis").get("error"));
		} finally {
			service.shutdown();
		}
	}

	@Test
	@DisplayName("Redis is skipped, and stays UP, when it is not configured")
	void checkHealth_skipsRedisWhenItIsNotConfigured() throws Exception {
		HealthService service = new HealthService(healthyDataSource(0, 0), null);
		try {
			Map<String, Object> health = service.checkHealth();

			assertEquals("UP", health.get("status"));
			assertEquals("Redis not configured — skipped", componentOf(health, "redis").get("message"));
		} finally {
			service.shutdown();
		}
	}

	@Test
	@DisplayName("a component that never ran is reported as DOWN")
	void checkHealth_reportsAComponentThatNeverRanAsDown() throws Exception {
		HealthService service = new HealthService(healthyDataSource(0, 0), redisAnswering("PONG"));
		service.shutdown();

		Map<String, Object> health = service.checkHealth();

		assertEquals("DOWN", health.get("status"));
		assertTrue(((String) componentOf(health, "mysql").get("error")).contains("did not complete in time"));
		assertTrue(((String) componentOf(health, "redis").get("error")).contains("did not complete in time"));
	}

	@Test
	@DisplayName("a pool that is nearly exhausted degrades the database")
	void checkHealth_reportsMysqlDegradedWhenTheConnectionPoolIsNearlyExhausted() throws Exception {
		HikariDataSource dataSource = mock(HikariDataSource.class);
		when(dataSource.getConnection()).thenAnswer(invocation -> connection(0, 0));
		HikariPoolMXBean poolMXBean = mock(HikariPoolMXBean.class);
		when(poolMXBean.getActiveConnections()).thenReturn(9);
		when(dataSource.getHikariPoolMXBean()).thenReturn(poolMXBean);
		when(dataSource.getMaximumPoolSize()).thenReturn(10);

		HealthService service = new HealthService(dataSource, redisAnswering("PONG"));
		try {
			assertEquals("DEGRADED", service.checkHealth().get("status"));
		} finally {
			service.shutdown();
		}
	}

	@Test
	@DisplayName("a pool with headroom leaves the database healthy")
	void checkHealth_leavesMysqlHealthyWhenTheConnectionPoolHasHeadroom() throws Exception {
		HikariDataSource dataSource = mock(HikariDataSource.class);
		when(dataSource.getConnection()).thenAnswer(invocation -> connection(0, 0));
		HikariPoolMXBean poolMXBean = mock(HikariPoolMXBean.class);
		when(poolMXBean.getActiveConnections()).thenReturn(1);
		when(dataSource.getHikariPoolMXBean()).thenReturn(poolMXBean);
		when(dataSource.getMaximumPoolSize()).thenReturn(10);

		HealthService service = new HealthService(dataSource, redisAnswering("PONG"));
		try {
			assertEquals("UP", service.checkHealth().get("status"));
		} finally {
			service.shutdown();
		}
	}

	@Test
	@DisplayName("unreadable pool metrics leave the database healthy")
	void checkHealth_leavesMysqlHealthyWhenThePoolMetricsCannotBeRead() throws Exception {
		HikariDataSource dataSource = mock(HikariDataSource.class);
		when(dataSource.getConnection()).thenAnswer(invocation -> connection(0, 0));
		when(dataSource.getHikariPoolMXBean()).thenThrow(new IllegalStateException("pool not started"));

		HealthService service = new HealthService(dataSource, redisAnswering("PONG"));
		try {
			assertEquals("UP", service.checkHealth().get("status"));
		} finally {
			service.shutdown();
		}
	}

	@Test
	@DisplayName("a database that fails mid-diagnosis is reported as degraded")
	void checkHealth_reportsMysqlDegradedWhenTheDiagnosticQueriesFail() throws Exception {
		DataSource dataSource = mock(DataSource.class);
		when(dataSource.getConnection()).thenAnswer(new org.mockito.stubbing.Answer<Connection>() {
			private int call = 0;

			@Override
			public Connection answer(org.mockito.invocation.InvocationOnMock invocation) throws Throwable {
				// The first connection serves the basic health query; the diagnostics that
				// follow cannot get one.
				if (call++ == 0) {
					return connection(0, 0);
				}
				throw new SQLException("pool exhausted");
			}
		});

		HealthService service = new HealthService(dataSource, redisAnswering("PONG"));
		try {
			assertEquals("DEGRADED", service.checkHealth().get("status"));
		} finally {
			service.shutdown();
		}
	}

	@Test
	@DisplayName("shutdown is safe to call twice")
	void shutdown_isSafeToCallTwice() throws Exception {
		HealthService service = new HealthService(healthyDataSource(0, 0), redisAnswering("PONG"));

		service.shutdown();
		service.shutdown();
	}

	@Test
	@DisplayName("a slow component is flagged with a warning severity")
	void checkHealth_flagsASlowComponentWithAWarningSeverity() throws Exception {
		HealthService service = new HealthService(healthyDataSource(0, 0), redisAnswering("PONG"));
		try {
			String severity = (String) ReflectionTestUtils.invokeMethod(service, "determineSeverity", true, 5000L,
					false);
			assertEquals("WARNING", severity);
			assertEquals("OK", ReflectionTestUtils.invokeMethod(service, "determineSeverity", true, 10L, false));
			assertEquals("CRITICAL", ReflectionTestUtils.invokeMethod(service, "determineSeverity", false, 10L,
					false));
		} finally {
			service.shutdown();
		}
	}
}
