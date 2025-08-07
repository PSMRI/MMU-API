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
package com.iemr.mmu.service.dataSyncLayerCentral;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DataSyncRepositoryCentralTest {
    @Mock
    private DataSource dataSource;
    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private DataSyncRepositoryCentral repository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = spy(new DataSyncRepositoryCentral());
        doReturn(jdbcTemplate).when(repository).getJdbcTemplate();
    }

    @Test
    void testCheckRecordIsAlreadyPresentOrNot_withSyncFacilityID() {
        String schema = "schema";
        String table = "t_patientissue";
        String vanSerialNo = "VSN";
        String vanID = "VID";
        String vanAutoIncColumnName = "id";
        int syncFacilityID = 1;
        List<Map<String, Object>> result = new ArrayList<>();
        result.add(new HashMap<>());
        when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(result);
        int res = repository.checkRecordIsAlreadyPresentOrNot(schema, table, vanSerialNo, vanID, vanAutoIncColumnName, syncFacilityID);
        assertEquals(1, res);
    }

    @Test
    void testCheckRecordIsAlreadyPresentOrNot_withVanID() {
        String schema = "schema";
        String table = "other_table";
        String vanSerialNo = "VSN";
        String vanID = "VID";
        String vanAutoIncColumnName = "id";
        int syncFacilityID = 0;
        List<Map<String, Object>> result = new ArrayList<>();
        when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(result);
        int res = repository.checkRecordIsAlreadyPresentOrNot(schema, table, vanSerialNo, vanID, vanAutoIncColumnName, syncFacilityID);
        assertEquals(0, res);
    }

    @Test
    void testSyncDataToCentralDB_insert() {
        String schema = "schema";
        String table = "table";
        String serverColumns = "col1,col2";
        String query = "INSERT INTO table VALUES (?,?)";
        List<Object[]> syncDataList = new ArrayList<>();
        syncDataList.add(new Object[]{"val1", "val2"});
        int[] expected = new int[]{1};
        when(jdbcTemplate.batchUpdate(eq(query), anyList())).thenReturn(expected);
        int[] result = repository.syncDataToCentralDB(schema, table, serverColumns, query, syncDataList);
        assertArrayEquals(expected, result);
    }

    @Test
    void testSyncDataToCentralDB_update() {
        String schema = "schema";
        String table = "table";
        String serverColumns = "col1,col2";
        String query = "UPDATE table SET col1=? WHERE col2=?";
        List<Object[]> syncDataList = new ArrayList<>();
        syncDataList.add(new Object[]{"val1", "val2"});
        int[] expected = new int[]{1};
        when(jdbcTemplate.batchUpdate(eq(query), anyList())).thenReturn(expected);
        int[] result = repository.syncDataToCentralDB(schema, table, serverColumns, query, syncDataList);
        assertArrayEquals(expected, result);
    }

    @Test
    void testGetMasterDataFromTable_allBranches() throws Exception {
        String schema = "schema";
        String table = "table";
        String columnNames = "col1,col2";
        Timestamp lastDownloadDate = new Timestamp(System.currentTimeMillis());
        Integer vanID = 1;
        Integer psmID = 2;
        List<Map<String, Object>> result = new ArrayList<>();
        when(jdbcTemplate.queryForList(anyString(), any(Timestamp.class))).thenReturn(result);
        when(jdbcTemplate.queryForList(anyString(), any(Timestamp.class), anyInt())).thenReturn(result);
        when(jdbcTemplate.queryForList(anyString(), any(Timestamp.class), anyInt())).thenReturn(result);
        when(jdbcTemplate.queryForList(anyString(), anyInt())).thenReturn(result);
        when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(result);
        when(jdbcTemplate.queryForList(anyString())).thenReturn(result);
        // masterType = "A", lastDownloadDate != null
        assertEquals(result, repository.getMasterDataFromTable(schema, table, columnNames, "A", lastDownloadDate, vanID, psmID));
        // masterType = "V", lastDownloadDate != null
        assertEquals(result, repository.getMasterDataFromTable(schema, table, columnNames, "V", lastDownloadDate, vanID, psmID));
        // masterType = "P", lastDownloadDate != null
        assertEquals(result, repository.getMasterDataFromTable(schema, table, columnNames, "P", lastDownloadDate, vanID, psmID));
        // masterType = "A", lastDownloadDate == null
        assertEquals(result, repository.getMasterDataFromTable(schema, table, columnNames, "A", null, vanID, psmID));
        // masterType = "V", lastDownloadDate == null
        assertEquals(result, repository.getMasterDataFromTable(schema, table, columnNames, "V", null, vanID, psmID));
        // masterType = "P", lastDownloadDate == null
        assertEquals(result, repository.getMasterDataFromTable(schema, table, columnNames, "P", null, vanID, psmID));
        // masterType = null
        assertEquals(Collections.emptyList(), repository.getMasterDataFromTable(schema, table, columnNames, null, lastDownloadDate, vanID, psmID));
    }
}
