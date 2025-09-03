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

import com.iemr.mmu.data.syncActivity_syncLayer.SyncDownloadMaster;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.sql.Timestamp;

@ExtendWith(MockitoExtension.class)
class GetMasterDataFromCentralForVanImplTest {
    @InjectMocks
    private GetMasterDataFromCentralForVanImpl service;

    @Mock
    private DataSyncRepositoryCentral dataSyncRepositoryCentral;

    @Test
    void testGetMasterDataForVan_valid() throws Exception {
        SyncDownloadMaster obj = mock(SyncDownloadMaster.class);
        when(obj.getSchemaName()).thenReturn("schema");
        when(obj.getTableName()).thenReturn("table");
        when(obj.getServerColumnName()).thenReturn("col");
        when(obj.getMasterType()).thenReturn("type");
        Timestamp ts = mock(Timestamp.class);
        when(obj.getLastDownloadDate()).thenReturn(ts);
        when(obj.getVanID()).thenReturn(Integer.valueOf(1));
        when(obj.getProviderServiceMapID()).thenReturn(Integer.valueOf(2));
        List<Map<String, Object>> mockList = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("key", "value");
        mockList.add(row);
        when(dataSyncRepositoryCentral.getMasterDataFromTable(anyString(), anyString(), anyString(), anyString(), any(Timestamp.class), anyInt(), anyInt())).thenReturn(mockList);
        String result = service.getMasterDataForVan(obj);
        assertNotNull(result);
        assertTrue(result.contains("key"));
    }

    @Test
    void testGetMasterDataForVan_invalid_nullObj() throws Exception {
        String result = service.getMasterDataForVan(null);
        assertNull(result);
    }

    @Test
    void testGetMasterDataForVan_invalid_missingSchemaOrTable() throws Exception {
        SyncDownloadMaster obj = mock(SyncDownloadMaster.class);
        when(obj.getSchemaName()).thenReturn(null);
        when(obj.getTableName()).thenReturn("table");
        String result = service.getMasterDataForVan(obj);
        assertNull(result);
        when(obj.getSchemaName()).thenReturn("schema");
        when(obj.getTableName()).thenReturn(null);
        result = service.getMasterDataForVan(obj);
        assertNull(result);
    }
}
