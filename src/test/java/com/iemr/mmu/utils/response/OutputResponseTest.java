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
package com.iemr.mmu.utils.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.ConnectException;
import java.sql.SQLException;
import java.text.ParseException;

import org.json.JSONException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.iemr.mmu.utils.exception.IEMRException;

class OutputResponseTest {

	@Test
	@DisplayName("a JSON object response is passed through as data")
	void setResponse_keepsAJsonObjectAsData() {
		OutputResponse response = new OutputResponse();
		response.setResponse("{\"name\":\"Asha\"}");

		assertTrue(response.isSuccess());
		assertEquals(OutputResponse.SUCCESS, response.getStatusCode());
		assertEquals("Success", response.getErrorMessage());
		assertEquals("{\"name\":\"Asha\"}", response.getData());
	}

	@Test
	@DisplayName("a JSON array response is passed through as data")
	void setResponse_keepsAJsonArrayAsData() {
		OutputResponse response = new OutputResponse();
		response.setResponse("[{\"name\":\"Asha\"}]");

		assertTrue(response.getData().startsWith("["));
	}

	@Test
	@DisplayName("a plain message response is wrapped in a response object")
	void setResponse_wrapsAPlainMessage() {
		OutputResponse response = new OutputResponse();
		response.setResponse("Data saved successfully");

		assertTrue(response.getData().contains("Data saved successfully"));
		assertTrue(response.getData().contains("response"));
	}

	@Test
	@DisplayName("each known exception type maps to its own status code")
	void setError_mapsEachKnownExceptionToItsStatusCode() {
		assertEquals(OutputResponse.USERID_FAILURE, errorFor(new IEMRException("bad user")));
		assertEquals(OutputResponse.OBJECT_FAILURE, errorFor(new JSONException("bad json")));
		assertEquals(OutputResponse.CODE_EXCEPTION, errorFor(new SQLException("bad sql")));
		assertEquals(OutputResponse.CODE_EXCEPTION, errorFor(new ParseException("bad date", 0)));
		assertEquals(OutputResponse.CODE_EXCEPTION, errorFor(new NullPointerException("npe")));
		assertEquals(OutputResponse.ENVIRONMENT_EXCEPTION, errorFor(new IOException("io")));
		assertEquals(OutputResponse.ENVIRONMENT_EXCEPTION, errorFor(new ConnectException("refused")));
		assertEquals(OutputResponse.GENERIC_FAILURE, errorFor(new RuntimeException("boom")));
	}

	private int errorFor(Throwable thrown) {
		OutputResponse response = new OutputResponse();
		response.setError(thrown);
		return response.getStatusCode();
	}

	@Test
	@DisplayName("an explicit error carries its own code, message and status")
	void setError_carriesTheGivenCodeAndMessage() {
		OutputResponse response = new OutputResponse();
		response.setError(404, "Not found", "MISSING");

		assertEquals(404, response.getStatusCode());
		assertEquals("Not found", response.getErrorMessage());
		assertEquals("MISSING", response.getStatus());
		assertFalse(response.isSuccess());
	}

	@Test
	@DisplayName("an error without a status reuses the message as the status")
	void setError_reusesTheMessageAsTheStatus() {
		OutputResponse response = new OutputResponse();
		response.setError(400, "Bad request");

		assertEquals("Bad request", response.getStatus());
	}

	@Test
	@DisplayName("a failed response carries no data")
	void getData_returnsNothingForAFailedResponse() {
		assertNull(new OutputResponse().getData());
	}

	@Test
	@DisplayName("serialising with nulls keeps the empty data field")
	void toStringWithSerialization_keepsTheEmptyDataField() {
		assertTrue(new OutputResponse().toStringWithSerialization().contains("\"data\":null"));
	}
}
