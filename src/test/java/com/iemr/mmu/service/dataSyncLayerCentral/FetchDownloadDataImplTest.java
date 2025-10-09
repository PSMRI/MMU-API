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

import com.iemr.mmu.data.syncActivity_syncLayer.SyncUploadDataDigester;
import com.iemr.mmu.repo.syncActivity_syncLayer.IndentIssueRepo;
import com.iemr.mmu.repo.syncActivity_syncLayer.IndentOrderRepo;
import com.iemr.mmu.repo.syncActivity_syncLayer.IndentRepo;
import com.iemr.mmu.repo.syncActivity_syncLayer.ItemStockEntryRepo;
import com.iemr.mmu.repo.syncActivity_syncLayer.StockTransferRepo;
import com.iemr.mmu.data.syncActivity_syncLayer.Indent;
import com.iemr.mmu.data.syncActivity_syncLayer.IndentOrder;
import com.iemr.mmu.data.syncActivity_syncLayer.IndentIssue;
import com.iemr.mmu.data.syncActivity_syncLayer.T_StockTransfer;
import com.iemr.mmu.data.syncActivity_syncLayer.ItemStockEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class FetchDownloadDataImplTest {
    @InjectMocks
    private FetchDownloadDataImpl service;
    @Mock private IndentRepo indentRepo;
    @Mock private IndentOrderRepo indentOrderRepo;
    @Mock private IndentIssueRepo indentIssueRepo;
    @Mock private StockTransferRepo stockTransferRepo;
    @Mock private ItemStockEntryRepo itemStockEntryRepo;
    @Mock private DataSyncRepositoryCentral dataSyncRepositoryCentral;

    private SyncUploadDataDigester baseDigester(String table) {
        SyncUploadDataDigester digester = mock(SyncUploadDataDigester.class);
        when(digester.getSchemaName()).thenReturn("schema");
        when(digester.getTableName()).thenReturn(table);
        when(digester.getFacilityID()).thenReturn(Integer.valueOf(1));
        return digester;
    }

    @Test
    void testGetDownloadData_indent() throws Exception {
        SyncUploadDataDigester digester = baseDigester("t_indent");
        Indent indent = mock(Indent.class);
        when(indentRepo.findByFromFacilityIDAndProcessedNotAndSyncFacilityIDNotNullAndVanSerialNoNotNull(anyInt(), anyString())).thenReturn(new ArrayList<>(Collections.singletonList(indent)));
        String result = service.getDownloadData(digester);
        assertNotNull(result);
    }

    @Test
    void testGetDownloadData_indentorder() throws Exception {
        SyncUploadDataDigester digester = baseDigester("t_indentorder");
        IndentOrder indentOrder = mock(IndentOrder.class);
        when(indentOrderRepo.findByFromFacilityIDAndProcessedNotAndSyncFacilityIDNotNullAndVanSerialNoNotNull(anyInt(), anyString())).thenReturn(new ArrayList<>(Collections.singletonList(indentOrder)));
        String result = service.getDownloadData(digester);
        assertNotNull(result);
    }

    @Test
    void testGetDownloadData_indentissue() throws Exception {
        SyncUploadDataDigester digester = baseDigester("t_indentissue");
        IndentIssue indentIssue = mock(IndentIssue.class);
        when(indentIssueRepo.findByToFacilityIDAndProcessedNotAndSyncFacilityIDNotNullAndVanSerialNoNotNull(anyInt(), anyString())).thenReturn(new ArrayList<>(Collections.singletonList(indentIssue)));
        String result = service.getDownloadData(digester);
        assertNotNull(result);
    }

    @Test
    void testGetDownloadData_stocktransfer() throws Exception {
        SyncUploadDataDigester digester = baseDigester("t_stocktransfer");
        T_StockTransfer stockTransfer = mock(T_StockTransfer.class);
        when(stockTransferRepo.findByTransferToFacilityIDAndProcessedNotAndSyncFacilityIDNotNullAndVanSerialNoNotNull(anyInt(), anyString())).thenReturn(new ArrayList<>(Collections.singletonList(stockTransfer)));
        String result = service.getDownloadData(digester);
        assertNotNull(result);
    }

    @Test
    void testGetDownloadData_itemstockentry() throws Exception {
        SyncUploadDataDigester digester = baseDigester("t_itemstockentry");
        ItemStockEntry itemStockEntry = mock(ItemStockEntry.class);
        when(itemStockEntryRepo.findByFacilityIDAndProcessedNotAndSyncFacilityIDNotNullAndVanSerialNoNotNull(anyInt(), anyString())).thenReturn(new ArrayList<>(Collections.singletonList(itemStockEntry)));
        String result = service.getDownloadData(digester);
        assertNotNull(result);
    }

    @Test
    void testGetDownloadData_invalidTable() {
        SyncUploadDataDigester digester = baseDigester("invalid_table");
        Exception ex = assertThrows(Exception.class, () -> service.getDownloadData(digester));
        assertTrue(ex.getMessage().contains("invalid download request"));
    }

    @Test
    void testGetDownloadData_missingFields() {
        SyncUploadDataDigester digester = mock(SyncUploadDataDigester.class);
        when(digester.getSchemaName()).thenReturn(null);
        Exception ex = assertThrows(Exception.class, () -> service.getDownloadData(digester));
        assertTrue(ex.getMessage().contains("invalid download request"));
    }

    private SyncUploadDataDigester baseUpdateDigester(String table) {
        SyncUploadDataDigester digester = mock(SyncUploadDataDigester.class);
        when(digester.getSchemaName()).thenReturn("schema");
        when(digester.getTableName()).thenReturn(table);
        when(digester.getIds()).thenReturn(Collections.singletonList(1L));
        return digester;
    }

    @Test
    void testUpdateProcessedFlag_indent() throws Exception {
        SyncUploadDataDigester digester = baseUpdateDigester("t_indent");
        when(indentRepo.updateProcessedFlag(anyList())).thenReturn(1);
        int result = service.updateProcessedFlagPostSuccessfullDownload(digester);
        assertEquals(1, result);
    }

    @Test
    void testUpdateProcessedFlag_indentorder() throws Exception {
        SyncUploadDataDigester digester = baseUpdateDigester("t_indentorder");
        when(indentOrderRepo.updateProcessedFlag(anyList())).thenReturn(2);
        int result = service.updateProcessedFlagPostSuccessfullDownload(digester);
        assertEquals(2, result);
    }

    @Test
    void testUpdateProcessedFlag_indentissue() throws Exception {
        SyncUploadDataDigester digester = baseUpdateDigester("t_indentissue");
        when(indentIssueRepo.updateProcessedFlag(anyList())).thenReturn(3);
        int result = service.updateProcessedFlagPostSuccessfullDownload(digester);
        assertEquals(3, result);
    }

    @Test
    void testUpdateProcessedFlag_stocktransfer() throws Exception {
        SyncUploadDataDigester digester = baseUpdateDigester("t_stocktransfer");
        when(stockTransferRepo.updateProcessedFlag(anyList())).thenReturn(4);
        int result = service.updateProcessedFlagPostSuccessfullDownload(digester);
        assertEquals(4, result);
    }

    @Test
    void testUpdateProcessedFlag_itemstockentry() throws Exception {
        SyncUploadDataDigester digester = baseUpdateDigester("t_itemstockentry");
        when(itemStockEntryRepo.updateProcessedFlag(anyList())).thenReturn(5);
        int result = service.updateProcessedFlagPostSuccessfullDownload(digester);
        assertEquals(5, result);
    }

    @Test
    void testUpdateProcessedFlag_invalidTable() {
        SyncUploadDataDigester digester = baseUpdateDigester("invalid_table");
        Exception ex = assertThrows(Exception.class, () -> service.updateProcessedFlagPostSuccessfullDownload(digester));
        assertTrue(ex.getMessage().contains("invalid request"));
    }

    @Test
    void testUpdateProcessedFlag_missingFields() {
        SyncUploadDataDigester digester = mock(SyncUploadDataDigester.class);
        when(digester.getSchemaName()).thenReturn(null);
        Exception ex = assertThrows(Exception.class, () -> service.updateProcessedFlagPostSuccessfullDownload(digester));
        assertTrue(ex.getMessage().contains("invalid request"));
    }
}
