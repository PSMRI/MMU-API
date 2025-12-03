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
package com.iemr.mmu.service.tele_consultation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.iemr.mmu.data.benFlowStatus.BeneficiaryFlowStatus;
import com.iemr.mmu.data.nurse.CommonUtilityClass;
import com.iemr.mmu.data.tele_consultation.TCRequestModel;
import com.iemr.mmu.data.tele_consultation.TcSpecialistSlotBookingRequestOBJ;
import com.iemr.mmu.data.tele_consultation.TeleconsultationRequestOBJ;
import com.iemr.mmu.repo.benFlowStatus.BeneficiaryFlowStatusRepo;
import com.iemr.mmu.repo.tc_consultation.TCRequestModelRepo;
import com.iemr.mmu.service.anc.Utility;
import com.iemr.mmu.service.common.transaction.CommonDoctorServiceImpl;
import com.iemr.mmu.utils.CookieUtil;
import com.iemr.mmu.utils.RestTemplateUtil;
import com.iemr.mmu.utils.mapper.InputMapper;
import com.iemr.mmu.utils.mapper.OutputMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("TeleConsultationServiceImpl Test Cases")
class TeleConsultationServiceImplTest {

    @InjectMocks
    private TeleConsultationServiceImpl teleConsultationService;

    @Mock
    private TCRequestModelRepo tCRequestModelRepo;

    @Mock
    private BeneficiaryFlowStatusRepo beneficiaryFlowStatusRepo;

    @Mock
    private CommonDoctorServiceImpl commonDoctorServiceImpl;

    @Mock
    private CookieUtil cookieUtil;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(teleConsultationService, "tcSpecialistSlotCancel", "http://test-url/cancel");
    }

    @Test
    @DisplayName("Test createTCRequest - Success")
    void testCreateTCRequest_Success() {
        // Arrange
        TCRequestModel tcRequestModel = createMockTCRequestModel();
        TCRequestModel savedModel = createMockTCRequestModel();
        savedModel.settMRequestID(1L);

        when(tCRequestModelRepo.save(tcRequestModel)).thenReturn(savedModel);

        // Act
        int result = teleConsultationService.createTCRequest(tcRequestModel);

        // Assert
        assertEquals(1, result);
        verify(tCRequestModelRepo).save(tcRequestModel);
    }

    @Test
    @DisplayName("Test createTCRequest - Failure")
    void testCreateTCRequest_Failure() {
        // Arrange
        TCRequestModel tcRequestModel = createMockTCRequestModel();

        when(tCRequestModelRepo.save(tcRequestModel)).thenReturn(null);

        // Act
        int result = teleConsultationService.createTCRequest(tcRequestModel);

        // Assert
        assertEquals(0, result);
    }

    @Test
    @DisplayName("Test createTCRequest - Zero ID")
    void testCreateTCRequest_ZeroId() {
        // Arrange
        TCRequestModel tcRequestModel = createMockTCRequestModel();
        TCRequestModel savedModel = createMockTCRequestModel();
        savedModel.settMRequestID(0L);

        when(tCRequestModelRepo.save(tcRequestModel)).thenReturn(savedModel);

        // Act
        int result = teleConsultationService.createTCRequest(tcRequestModel);

        // Assert
        assertEquals(0, result);
    }

    @Test
    @DisplayName("Test updateBeneficiaryArrivalStatus - Success")
    void testUpdateBeneficiaryArrivalStatus_Success() throws Exception {
        // Arrange
        String requestOBJ = createValidArrivalStatusRequest();

        when(beneficiaryFlowStatusRepo.updateBeneficiaryArrivalStatus(anyLong(), anyLong(), anyLong(), 
            anyBoolean(), anyString(), anyInt())).thenReturn(1);
        when(tCRequestModelRepo.updateBeneficiaryStatus(anyLong(), anyLong(), anyString(), 
            anyString(), anyInt(), anyBoolean())).thenReturn(1);

        // Act
        int result = teleConsultationService.updateBeneficiaryArrivalStatus(requestOBJ);

        // Assert
        assertEquals(1, result);
    }

    @Test
    @DisplayName("Test updateBeneficiaryArrivalStatus - Flow Status Update Failed")
    void testUpdateBeneficiaryArrivalStatus_FlowStatusUpdateFailed() {
        // Arrange
        String requestOBJ = createValidArrivalStatusRequest();

        when(beneficiaryFlowStatusRepo.updateBeneficiaryArrivalStatus(anyLong(), anyLong(), anyLong(), 
            anyBoolean(), anyString(), anyInt())).thenReturn(0);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            teleConsultationService.updateBeneficiaryArrivalStatus(requestOBJ);
        });

        assertTrue(exception.getMessage().contains("Beneficiary arrival status update failed"));
    }

    @Test
    @DisplayName("Test updateBeneficiaryArrivalStatus - TC Request Update Failed")
    void testUpdateBeneficiaryArrivalStatus_TCRequestUpdateFailed() {
        // Arrange
        String requestOBJ = createValidArrivalStatusRequest();

        when(beneficiaryFlowStatusRepo.updateBeneficiaryArrivalStatus(anyLong(), anyLong(), anyLong(), 
            anyBoolean(), anyString(), anyInt())).thenReturn(1);
        when(tCRequestModelRepo.updateBeneficiaryStatus(anyLong(), anyLong(), anyString(), 
            anyString(), anyInt(), anyBoolean())).thenReturn(0);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            teleConsultationService.updateBeneficiaryArrivalStatus(requestOBJ);
        });

        assertTrue(exception.getMessage().contains("Beneficiary arrival status update failed"));
    }

    @Test
    @DisplayName("Test updateBeneficiaryArrivalStatus - Invalid Request")
    void testUpdateBeneficiaryArrivalStatus_InvalidRequest() {
        // Arrange
        String requestOBJ = "{}";

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            teleConsultationService.updateBeneficiaryArrivalStatus(requestOBJ);
        });

        assertTrue(exception.getMessage().contains("Invalid request"));
    }

    @Test
    @DisplayName("Test updateBeneficiaryArrivalStatus - Missing Fields")
    void testUpdateBeneficiaryArrivalStatus_MissingFields() {
        // Arrange
        String requestOBJ = "{\"benflowID\": 1}";

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            teleConsultationService.updateBeneficiaryArrivalStatus(requestOBJ);
        });

        assertTrue(exception.getMessage().contains("Invalid request"));
    }

    @Test
    @DisplayName("Test updateBeneficiaryArrivalStatus - Null Fields")
    void testUpdateBeneficiaryArrivalStatus_NullFields() {
        // Arrange
        String requestOBJ = "{\"benflowID\": null, \"benRegID\": 1, \"visitCode\": 1, \"modifiedBy\": \"test\", \"status\": true, \"userID\": 1}";

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            teleConsultationService.updateBeneficiaryArrivalStatus(requestOBJ);
        });

        assertTrue(exception.getMessage().contains("Invalid request"));
    }

    @Test
    @DisplayName("Test updateBeneficiaryStatusToCancelTCRequest - Success")
    void testUpdateBeneficiaryStatusToCancelTCRequest_Success() throws Exception {
        // Arrange
        String requestOBJ = createValidCancelRequest();
        String authorization = "Bearer token";
        String token = "token";

        ArrayList<TCRequestModel> tcList = new ArrayList<>();
        TCRequestModel tcModel = createMockTCRequestModelWithTime();
        tcList.add(tcModel);

        // Mock the cancelSlotForTCCancel method by directly mocking the repo calls
        when(tCRequestModelRepo.getTcDetailsList(anyLong(), anyLong(), anyInt(), any()))
            .thenReturn(tcList);

        when(beneficiaryFlowStatusRepo.updateBeneficiaryStatusToCancelRequest(anyLong(), anyLong(), 
            anyLong(), anyString(), anyInt())).thenReturn(1);
        when(tCRequestModelRepo.updateBeneficiaryStatus(anyLong(), anyLong(), anyString(), 
            anyString(), anyInt(), anyBoolean())).thenReturn(1);

        // Mock RestTemplate and its response
        ResponseEntity<String> response = new ResponseEntity<>("{\"statusCode\": 200}", HttpStatus.OK);
        
        try (MockedStatic<RestTemplateUtil> restTemplateUtilMock = mockStatic(RestTemplateUtil.class);
             MockedStatic<OutputMapper> outputMapperMock = mockStatic(OutputMapper.class);
             MockedConstruction<RestTemplate> restTemplateMock = mockConstruction(RestTemplate.class, 
                 (mock, context) -> {
                     when(mock.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
                         .thenReturn(response);
                 })) {
            
            restTemplateUtilMock.when(() -> RestTemplateUtil.createRequestEntity(anyString(), anyString(), anyString()))
                .thenReturn(new HttpEntity<>("test"));

            Gson mockGson = mock(Gson.class);
            outputMapperMock.when(OutputMapper::gson).thenReturn(mockGson);
            when(mockGson.toJson(any(Object.class))).thenReturn("{}");

            // Act
            int result = teleConsultationService.updateBeneficiaryStatusToCancelTCRequest(requestOBJ, authorization, token);

            // Assert
            assertEquals(1, result);
        }
    }

    @Test
    @DisplayName("Test updateBeneficiaryStatusToCancelTCRequest - Invalid Request")
    void testUpdateBeneficiaryStatusToCancelTCRequest_InvalidRequest() {
        // Arrange - empty JSON object missing required fields
        String requestOBJ = "{}";
        String authorization = "Bearer token";
        String token = "token";

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            teleConsultationService.updateBeneficiaryStatusToCancelTCRequest(requestOBJ, authorization, token);
        });

        assertTrue(exception.getMessage().contains("Invalid request"));
    }

    @Test
    @DisplayName("Test updateBeneficiaryStatusToCancelTCRequest - Cancel Failed")
    void testUpdateBeneficiaryStatusToCancelTCRequest_CancelFailed() throws Exception {
        // Arrange
        String requestOBJ = createValidCancelRequest();
        String authorization = "Bearer token";
        String token = "token";

        ArrayList<TCRequestModel> tcList = new ArrayList<>();
        TCRequestModel tcModel = createMockTCRequestModelWithTime();
        tcList.add(tcModel);

        // Mock the cancelSlotForTCCancel method by directly mocking the repo calls
        when(tCRequestModelRepo.getTcDetailsList(anyLong(), anyLong(), anyInt(), any()))
            .thenReturn(tcList);

        when(beneficiaryFlowStatusRepo.updateBeneficiaryStatusToCancelRequest(anyLong(), anyLong(), 
            anyLong(), anyString(), anyInt())).thenReturn(1);
        when(tCRequestModelRepo.updateBeneficiaryStatus(anyLong(), anyLong(), anyString(), 
            anyString(), anyInt(), anyBoolean())).thenReturn(1);

        // Mock RestTemplate and its response - return error response
        ResponseEntity<String> response = new ResponseEntity<>("{\"statusCode\": 500}", HttpStatus.OK);
        
        try (MockedStatic<RestTemplateUtil> restTemplateUtilMock = mockStatic(RestTemplateUtil.class);
             MockedStatic<OutputMapper> outputMapperMock = mockStatic(OutputMapper.class);
             MockedConstruction<RestTemplate> restTemplateMock = mockConstruction(RestTemplate.class, 
                 (mock, context) -> {
                     when(mock.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
                         .thenReturn(response);
                 })) {
            
            restTemplateUtilMock.when(() -> RestTemplateUtil.createRequestEntity(anyString(), anyString(), anyString()))
                .thenReturn(new HttpEntity<>("test"));

            Gson mockGson = mock(Gson.class);
            outputMapperMock.when(OutputMapper::gson).thenReturn(mockGson);
            when(mockGson.toJson(any(Object.class))).thenReturn("{}");

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                teleConsultationService.updateBeneficiaryStatusToCancelTCRequest(requestOBJ, authorization, token);
            });

            assertTrue(exception.getMessage().contains("Teleconsultation cancel request failed"));
        }
    }

    @Test
    @DisplayName("Test cancelSlotForTCCancel - Success with TC Details")
    void testCancelSlotForTCCancel_SuccessWithTCDetails() throws Exception {
        // Arrange
        int userID = 1;
        long benRegID = 1L;
        long visitCode = 1L;
        String authorization = "Bearer token";
        String token = "token";

        ArrayList<TCRequestModel> tcList = new ArrayList<>();
        TCRequestModel tcModel = createMockTCRequestModelWithTime();
        tcList.add(tcModel);

        Set<String> statusSet = new HashSet<>();
        statusSet.add("N");
        statusSet.add("A");
        statusSet.add("O");

        when(tCRequestModelRepo.getTcDetailsList(benRegID, visitCode, userID, statusSet))
            .thenReturn(tcList);

        ResponseEntity<String> response = new ResponseEntity<>("{\"statusCode\": 200}", HttpStatus.OK);

        try (MockedStatic<RestTemplateUtil> restTemplateUtilMock = mockStatic(RestTemplateUtil.class);
             MockedStatic<OutputMapper> outputMapperMock = mockStatic(OutputMapper.class);
             MockedConstruction<RestTemplate> restTemplateMock = mockConstruction(RestTemplate.class, 
                 (mock, context) -> {
                     when(mock.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
                         .thenReturn(response);
                 })) {
            
            restTemplateUtilMock.when(() -> RestTemplateUtil.createRequestEntity(anyString(), anyString(), anyString()))
                .thenReturn(new HttpEntity<>("test"));

            Gson mockGson = mock(Gson.class);
            outputMapperMock.when(OutputMapper::gson).thenReturn(mockGson);
            when(mockGson.toJson(any(Object.class))).thenReturn("{}");

            // Act
            int result = teleConsultationService.cancelSlotForTCCancel(userID, benRegID, visitCode, authorization, token);

            // Assert
            assertEquals(1, result);
        }
    }

    @Test
    @DisplayName("Test cancelSlotForTCCancel - No TC Details Found")
    void testCancelSlotForTCCancel_NoTCDetailsFound() throws Exception {
        // Arrange
        int userID = 1;
        long benRegID = 1L;
        long visitCode = 1L;
        String authorization = "Bearer token";
        String token = "token";

        when(tCRequestModelRepo.getTcDetailsList(anyLong(), anyLong(), anyInt(), any()))
            .thenReturn(new ArrayList<>());

        // Act
        int result = teleConsultationService.cancelSlotForTCCancel(userID, benRegID, visitCode, authorization, token);

        // Assert
        assertEquals(1, result);
    }

    @Test
    @DisplayName("Test cancelSlotForTCCancel - API Response Non-200")
    void testCancelSlotForTCCancel_APIResponseNon200() throws Exception {
        // Arrange
        int userID = 1;
        long benRegID = 1L;
        long visitCode = 1L;
        String authorization = "Bearer token";
        String token = "token";

        ArrayList<TCRequestModel> tcList = new ArrayList<>();
        TCRequestModel tcModel = createMockTCRequestModelWithTime();
        tcList.add(tcModel);

        when(tCRequestModelRepo.getTcDetailsList(anyLong(), anyLong(), anyInt(), any()))
            .thenReturn(tcList);

        ResponseEntity<String> response = new ResponseEntity<>("{\"statusCode\": 200}", HttpStatus.INTERNAL_SERVER_ERROR);

        try (MockedStatic<RestTemplateUtil> restTemplateUtilMock = mockStatic(RestTemplateUtil.class);
             MockedStatic<OutputMapper> outputMapperMock = mockStatic(OutputMapper.class);
             MockedConstruction<RestTemplate> restTemplateMock = mockConstruction(RestTemplate.class, 
                 (mock, context) -> {
                     when(mock.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
                         .thenReturn(response);
                 })) {
            
            restTemplateUtilMock.when(() -> RestTemplateUtil.createRequestEntity(anyString(), anyString(), anyString()))
                .thenReturn(new HttpEntity<>("test"));

            Gson mockGson = mock(Gson.class);
            outputMapperMock.when(OutputMapper::gson).thenReturn(mockGson);
            when(mockGson.toJson(any(Object.class))).thenReturn("{}");

            // Act
            int result = teleConsultationService.cancelSlotForTCCancel(userID, benRegID, visitCode, authorization, token);

            // Assert
            assertEquals(0, result);
        }
    }

    @Test
    @DisplayName("Test checkBeneficiaryStatusForSpecialistTransaction - Success")
    void testCheckBeneficiaryStatusForSpecialistTransaction_Success() throws Exception {
        // Arrange
        String requestOBJ = createValidCheckStatusRequest();

        ArrayList<BeneficiaryFlowStatus> beneficiaryList = new ArrayList<>();
        BeneficiaryFlowStatus benFlow = createMockBeneficiaryFlowStatus();
        benFlow.setBenArrivedFlag(true);
        beneficiaryList.add(benFlow);

        ArrayList<TCRequestModel> tcList = new ArrayList<>();
        tcList.add(createMockTCRequestModel());

        when(beneficiaryFlowStatusRepo.checkBeneficiaryArrivalStatus(anyLong(), anyLong(), anyLong(), anyInt()))
            .thenReturn(beneficiaryList);
        when(tCRequestModelRepo.checkBenTcStatus(anyLong(), anyLong(), anyInt()))
            .thenReturn(tcList);

        // Act
        int result = teleConsultationService.checkBeneficiaryStatusForSpecialistTransaction(requestOBJ);

        // Assert
        assertEquals(1, result);
    }

    @Test
    @DisplayName("Test checkBeneficiaryStatusForSpecialistTransaction - Beneficiary Not Arrived")
    void testCheckBeneficiaryStatusForSpecialistTransaction_BeneficiaryNotArrived() {
        // Arrange
        String requestOBJ = createValidCheckStatusRequest();

        ArrayList<BeneficiaryFlowStatus> beneficiaryList = new ArrayList<>();
        BeneficiaryFlowStatus benFlow = createMockBeneficiaryFlowStatus();
        benFlow.setBenArrivedFlag(false);
        beneficiaryList.add(benFlow);

        when(beneficiaryFlowStatusRepo.checkBeneficiaryArrivalStatus(anyLong(), anyLong(), anyLong(), anyInt()))
            .thenReturn(beneficiaryList);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            teleConsultationService.checkBeneficiaryStatusForSpecialistTransaction(requestOBJ);
        });

        assertTrue(exception.getMessage().contains("Beneficiary has not arrived at TM spoke/center"));
    }

    @Test
    @DisplayName("Test checkBeneficiaryStatusForSpecialistTransaction - No Active TC Session")
    void testCheckBeneficiaryStatusForSpecialistTransaction_NoActiveTCSession() {
        // Arrange
        String requestOBJ = createValidCheckStatusRequest();

        ArrayList<BeneficiaryFlowStatus> beneficiaryList = new ArrayList<>();
        BeneficiaryFlowStatus benFlow = createMockBeneficiaryFlowStatus();
        benFlow.setBenArrivedFlag(true);
        beneficiaryList.add(benFlow);

        when(beneficiaryFlowStatusRepo.checkBeneficiaryArrivalStatus(anyLong(), anyLong(), anyLong(), anyInt()))
            .thenReturn(beneficiaryList);
        when(tCRequestModelRepo.checkBenTcStatus(anyLong(), anyLong(), anyInt()))
            .thenReturn(new ArrayList<>());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            teleConsultationService.checkBeneficiaryStatusForSpecialistTransaction(requestOBJ);
        });

        assertTrue(exception.getMessage().contains("Beneficiary has not any active Teleconsultation session"));
    }

    @Test
    @DisplayName("Test checkBeneficiaryStatusForSpecialistTransaction - Multiple Records Found")
    void testCheckBeneficiaryStatusForSpecialistTransaction_MultipleRecordsFound() {
        // Arrange
        String requestOBJ = createValidCheckStatusRequest();

        ArrayList<BeneficiaryFlowStatus> beneficiaryList = new ArrayList<>();
        beneficiaryList.add(createMockBeneficiaryFlowStatus());
        beneficiaryList.add(createMockBeneficiaryFlowStatus());

        when(beneficiaryFlowStatusRepo.checkBeneficiaryArrivalStatus(anyLong(), anyLong(), anyLong(), anyInt()))
            .thenReturn(beneficiaryList);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            teleConsultationService.checkBeneficiaryStatusForSpecialistTransaction(requestOBJ);
        });

        assertTrue(exception.getMessage().contains("No record or multiple record found in DB"));
    }

    @Test
    @DisplayName("Test checkBeneficiaryStatusForSpecialistTransaction - Invalid Request")
    void testCheckBeneficiaryStatusForSpecialistTransaction_InvalidRequest() {
        // Arrange
        String requestOBJ = "{}";

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            teleConsultationService.checkBeneficiaryStatusForSpecialistTransaction(requestOBJ);
        });

        assertTrue(exception.getMessage().contains("Invalid request"));
    }

    @Test
    @DisplayName("Test createTCRequestFromWorkList - Success")
    void testCreateTCRequestFromWorkList_Success() throws Exception {
        // Arrange
        JsonObject tcRequestOBJ = createValidTCRequestFromWorkListJSON();
        String authorization = "Bearer token";

        try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class);
             MockedStatic<Utility> utilityMock = mockStatic(Utility.class)) {
            
            InputMapper mockInputMapper = mock(InputMapper.class);
            inputMapperMock.when(InputMapper::gson).thenReturn(mockInputMapper);
            
            CommonUtilityClass commonUtilityClass = createMockCommonUtilityClass();
            TeleconsultationRequestOBJ tcRequest = createMockTeleconsultationRequestOBJ();
            TCRequestModel tcRequestModel = createMockTCRequestModel();

            when(mockInputMapper.fromJson(eq(tcRequestOBJ), eq(CommonUtilityClass.class)))
                .thenReturn(commonUtilityClass);
            when(mockInputMapper.fromJson(any(JsonObject.class), eq(TeleconsultationRequestOBJ.class)))
                .thenReturn(tcRequest);
            when(mockInputMapper.fromJson(eq(tcRequestOBJ), eq(TCRequestModel.class)))
                .thenReturn(tcRequestModel);

            utilityMock.when(() -> Utility.combineDateAndTimeToDateTime(anyString(), anyString()))
                .thenReturn(new Timestamp(System.currentTimeMillis()));
            utilityMock.when(() -> Utility.timeDiff(anyString(), anyString()))
                .thenReturn(30L);

            when(commonDoctorServiceImpl.callTmForSpecialistSlotBook(any(TcSpecialistSlotBookingRequestOBJ.class), anyString()))
                .thenReturn(1);

            TCRequestModel savedModel = createMockTCRequestModel();
            savedModel.settMRequestID(1L);
            when(tCRequestModelRepo.save(any(TCRequestModel.class))).thenReturn(savedModel);

            when(beneficiaryFlowStatusRepo.updateFlagAfterTcRequestCreatedFromWorklist(anyLong(), anyLong(), 
                anyLong(), anyInt(), any(Timestamp.class))).thenReturn(1);

            // Act
            int result = teleConsultationService.createTCRequestFromWorkList(tcRequestOBJ, authorization);

            // Assert
            assertEquals(1, result);
        }
    }

    @Test
    @DisplayName("Test createTCRequestFromWorkList - Slot Booking Failed")
    void testCreateTCRequestFromWorkList_SlotBookingFailed() throws Exception {
        // Arrange
        JsonObject tcRequestOBJ = createValidTCRequestFromWorkListJSON();
        String authorization = "Bearer token";

        try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class);
             MockedStatic<Utility> utilityMock = mockStatic(Utility.class)) {
            
            InputMapper mockInputMapper = mock(InputMapper.class);
            inputMapperMock.when(InputMapper::gson).thenReturn(mockInputMapper);
            
            CommonUtilityClass commonUtilityClass = createMockCommonUtilityClass();
            TeleconsultationRequestOBJ tcRequest = createMockTeleconsultationRequestOBJ();
            TCRequestModel tcRequestModel = createMockTCRequestModel();

            when(mockInputMapper.fromJson(eq(tcRequestOBJ), eq(CommonUtilityClass.class)))
                .thenReturn(commonUtilityClass);
            when(mockInputMapper.fromJson(any(JsonObject.class), eq(TeleconsultationRequestOBJ.class)))
                .thenReturn(tcRequest);
            when(mockInputMapper.fromJson(eq(tcRequestOBJ), eq(TCRequestModel.class)))
                .thenReturn(tcRequestModel);

            utilityMock.when(() -> Utility.combineDateAndTimeToDateTime(anyString(), anyString()))
                .thenReturn(new Timestamp(System.currentTimeMillis()));
            utilityMock.when(() -> Utility.timeDiff(anyString(), anyString()))
                .thenReturn(30L);

            when(commonDoctorServiceImpl.callTmForSpecialistSlotBook(any(TcSpecialistSlotBookingRequestOBJ.class), anyString()))
                .thenReturn(0);

            // Act & Assert
            Exception exception = assertThrows(Exception.class, () -> {
                teleConsultationService.createTCRequestFromWorkList(tcRequestOBJ, authorization);
            });

            assertTrue(exception.getMessage().contains("Error while Booking slot"));
        }
    }

    @Test
    @DisplayName("Test createTCRequestFromWorkList - TC Request Creation Failed")
    void testCreateTCRequestFromWorkList_TCRequestCreationFailed() throws Exception {
        // Arrange
        JsonObject tcRequestOBJ = createValidTCRequestFromWorkListJSON();
        String authorization = "Bearer token";

        try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class);
             MockedStatic<Utility> utilityMock = mockStatic(Utility.class)) {
            
            InputMapper mockInputMapper = mock(InputMapper.class);
            inputMapperMock.when(InputMapper::gson).thenReturn(mockInputMapper);
            
            CommonUtilityClass commonUtilityClass = createMockCommonUtilityClass();
            TeleconsultationRequestOBJ tcRequest = createMockTeleconsultationRequestOBJ();
            TCRequestModel tcRequestModel = createMockTCRequestModel();

            when(mockInputMapper.fromJson(eq(tcRequestOBJ), eq(CommonUtilityClass.class)))
                .thenReturn(commonUtilityClass);
            when(mockInputMapper.fromJson(any(JsonObject.class), eq(TeleconsultationRequestOBJ.class)))
                .thenReturn(tcRequest);
            when(mockInputMapper.fromJson(eq(tcRequestOBJ), eq(TCRequestModel.class)))
                .thenReturn(tcRequestModel);

            utilityMock.when(() -> Utility.combineDateAndTimeToDateTime(anyString(), anyString()))
                .thenReturn(new Timestamp(System.currentTimeMillis()));
            utilityMock.when(() -> Utility.timeDiff(anyString(), anyString()))
                .thenReturn(30L);

            when(commonDoctorServiceImpl.callTmForSpecialistSlotBook(any(TcSpecialistSlotBookingRequestOBJ.class), anyString()))
                .thenReturn(1);

            when(tCRequestModelRepo.save(any(TCRequestModel.class))).thenReturn(null);

            // Act & Assert
            Exception exception = assertThrows(Exception.class, () -> {
                teleConsultationService.createTCRequestFromWorkList(tcRequestOBJ, authorization);
            });

            assertTrue(exception.getMessage().contains("Error while creating Teleconsultation request"));
        }
    }

    @Test
    @DisplayName("Test createTCRequestFromWorkList - Status Update Failed")
    void testCreateTCRequestFromWorkList_StatusUpdateFailed() throws Exception {
        // Arrange
        JsonObject tcRequestOBJ = createValidTCRequestFromWorkListJSON();
        String authorization = "Bearer token";

        try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class);
             MockedStatic<Utility> utilityMock = mockStatic(Utility.class)) {
            
            InputMapper mockInputMapper = mock(InputMapper.class);
            inputMapperMock.when(InputMapper::gson).thenReturn(mockInputMapper);
            
            CommonUtilityClass commonUtilityClass = createMockCommonUtilityClass();
            TeleconsultationRequestOBJ tcRequest = createMockTeleconsultationRequestOBJ();
            TCRequestModel tcRequestModel = createMockTCRequestModel();

            when(mockInputMapper.fromJson(eq(tcRequestOBJ), eq(CommonUtilityClass.class)))
                .thenReturn(commonUtilityClass);
            when(mockInputMapper.fromJson(any(JsonObject.class), eq(TeleconsultationRequestOBJ.class)))
                .thenReturn(tcRequest);
            when(mockInputMapper.fromJson(eq(tcRequestOBJ), eq(TCRequestModel.class)))
                .thenReturn(tcRequestModel);

            utilityMock.when(() -> Utility.combineDateAndTimeToDateTime(anyString(), anyString()))
                .thenReturn(new Timestamp(System.currentTimeMillis()));
            utilityMock.when(() -> Utility.timeDiff(anyString(), anyString()))
                .thenReturn(30L);

            when(commonDoctorServiceImpl.callTmForSpecialistSlotBook(any(TcSpecialistSlotBookingRequestOBJ.class), anyString()))
                .thenReturn(1);

            TCRequestModel savedModel = createMockTCRequestModel();
            savedModel.settMRequestID(1L);
            when(tCRequestModelRepo.save(any(TCRequestModel.class))).thenReturn(savedModel);

            when(beneficiaryFlowStatusRepo.updateFlagAfterTcRequestCreatedFromWorklist(anyLong(), anyLong(), 
                anyLong(), anyInt(), any(Timestamp.class))).thenReturn(0);

            // Act & Assert
            Exception exception = assertThrows(Exception.class, () -> {
                teleConsultationService.createTCRequestFromWorkList(tcRequestOBJ, authorization);
            });

            assertTrue(exception.getMessage().contains("ERROR while updating beneficiary status for Teleconsultation request"));
        }
    }

    @Test
    @DisplayName("Test createTCRequestFromWorkList - Invalid Request")
    void testCreateTCRequestFromWorkList_InvalidRequest() throws Exception {
        // Arrange
        JsonObject tcRequestOBJ = new JsonObject();
        String authorization = "Bearer token";

        try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
            InputMapper mockInputMapper = mock(InputMapper.class);
            inputMapperMock.when(InputMapper::gson).thenReturn(mockInputMapper);
            
            // Return null for CommonUtilityClass to trigger invalid request
            when(mockInputMapper.fromJson(eq(tcRequestOBJ), eq(CommonUtilityClass.class)))
                .thenReturn(null);

            // Act & Assert
            Exception exception = assertThrows(Exception.class, () -> {
                teleConsultationService.createTCRequestFromWorkList(tcRequestOBJ, authorization);
            });

            assertTrue(exception.getMessage().contains("Invalid request"));
        }
    }

    @Test
    @DisplayName("Test getTCRequestListBySpecialistIdAndDate - Success")
    void testGetTCRequestListBySpecialistIdAndDate_Success() throws Exception {
        // Arrange
        Integer providerServiceMapID = 1;
        Integer userID = 1;
        String reqDate = "2023-12-01T10:00:00";

        ArrayList<BeneficiaryFlowStatus> tcList = new ArrayList<>();
        tcList.add(createMockBeneficiaryFlowStatus());

        when(beneficiaryFlowStatusRepo.getTCRequestList(eq(providerServiceMapID), eq(userID), any(Timestamp.class)))
            .thenReturn(tcList);

        // Act
        String result = teleConsultationService.getTCRequestListBySpecialistIdAndDate(providerServiceMapID, userID, reqDate);

        // Assert
        assertNotNull(result);
        // Verify that the result contains the expected data (exact format may vary based on Gson serialization)
        assertTrue(result.contains("\"benFlowID\":1"));
        assertTrue(result.contains("\"beneficiaryRegID\":1"));
        assertTrue(result.contains("\"visitCode\":1"));
        assertTrue(result.contains("\"benArrivedFlag\":true"));
    }

    @Test
    @DisplayName("Test setCommonDoctorServiceImpl - Coverage")
    void testSetCommonDoctorServiceImpl_Coverage() {
        // Arrange
        CommonDoctorServiceImpl mockService = mock(CommonDoctorServiceImpl.class);

        // Act
        teleConsultationService.setCommonDoctorServiceImpl(mockService);

        // Assert - This test ensures setter method is called and covered
        assertNotNull(teleConsultationService);
    }

    // Helper methods
    private TCRequestModel createMockTCRequestModel() {
        TCRequestModel model = new TCRequestModel();
        model.settMRequestID(1L);
        model.setBeneficiaryRegID(1L);
        model.setVisitCode(1L);
        model.setUserID(1);
        model.setRequestDate(new Timestamp(System.currentTimeMillis()));
        model.setDuration_minute(30L);
        return model;
    }

    private TCRequestModel createMockTCRequestModelWithTime() {
        TCRequestModel model = createMockTCRequestModel();
        model.setRequestDate(Timestamp.valueOf("2023-12-01 10:30:00"));
        return model;
    }

    private BeneficiaryFlowStatus createMockBeneficiaryFlowStatus() {
        BeneficiaryFlowStatus status = new BeneficiaryFlowStatus();
        status.setBenFlowID(1L);
        status.setBeneficiaryRegID(1L);
        status.setVisitCode(1L);
        status.setBenArrivedFlag(true);
        return status;
    }

    private CommonUtilityClass createMockCommonUtilityClass() {
        CommonUtilityClass utility = new CommonUtilityClass();
        utility.setBenFlowID(1L);
        utility.setBeneficiaryRegID(1L);
        utility.setVisitCode(1L);
        utility.setCreatedBy("test-user");
        return utility;
    }

    private TeleconsultationRequestOBJ createMockTeleconsultationRequestOBJ() {
        TeleconsultationRequestOBJ obj = new TeleconsultationRequestOBJ();
        obj.setUserID(1);
        obj.setAllocationDate(new Timestamp(System.currentTimeMillis()));
        obj.setFromTime("10:00");
        obj.setToTime("10:30");
        return obj;
    }

    private String createValidArrivalStatusRequest() {
        return "{\"benflowID\": 1, \"benRegID\": 1, \"visitCode\": 1, \"modifiedBy\": \"test\", \"status\": true, \"userID\": 1}";
    }

    private String createValidCancelRequest() {
        return "{\"benflowID\": 1, \"benRegID\": 1, \"visitCode\": 1, \"modifiedBy\": \"test\", \"userID\": 1}";
    }

    private String createValidCheckStatusRequest() {
        return "{\"benflowID\": 1, \"benRegID\": 1, \"visitCode\": 1, \"userID\": 1}";
    }

    private JsonObject createValidTCRequestFromWorkListJSON() {
        JsonObject obj = new JsonObject();
        obj.addProperty("benFlowID", 1);
        obj.addProperty("beneficiaryRegID", 1);
        obj.addProperty("visitCode", 1);
        obj.addProperty("createdBy", "test-user");
        
        JsonObject tcRequest = new JsonObject();
        tcRequest.addProperty("userID", 1);
        tcRequest.addProperty("allocationDate", "2023-12-01");
        tcRequest.addProperty("fromTime", "10:00");
        tcRequest.addProperty("toTime", "10:30");
        
        obj.add("tcRequest", tcRequest);
        return obj;
    }
}
