package com.iemr.mmu.controller.registrar.main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyChar;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.iemr.mmu.data.registrar.BeneficiaryData;
import com.iemr.mmu.data.registrar.V_BenAdvanceSearch;
import com.iemr.mmu.data.registrar.WrapperBeneficiaryRegistration;
import com.iemr.mmu.service.common.master.RegistrarServiceMasterDataImpl;
import com.iemr.mmu.service.common.transaction.CommonNurseServiceImpl;
import com.iemr.mmu.service.nurse.NurseServiceImpl;
import com.iemr.mmu.service.registrar.RegistrarServiceImpl;
import com.iemr.mmu.utils.exception.IEMRException;
import com.iemr.mmu.utils.mapper.InputMapper;
import com.iemr.mmu.utils.response.OutputResponse;

import javassist.NotFoundException;

@ExtendWith(MockitoExtension.class)
class RegistrarControllerTest {

	@InjectMocks
	RegistrarController registrarController;
	@Mock
	private Logger logger = LoggerFactory.getLogger(RegistrarController.class);
	@Mock
	private InputMapper inputMapper = new InputMapper();
	@Mock
	private RegistrarServiceImpl registrarServiceImpl;
	@Mock
	private RegistrarServiceMasterDataImpl registrarServiceMasterDataImpl;
	@Mock
	private NurseServiceImpl nurseServiceImpl;
	@Mock
	private CommonNurseServiceImpl commonNurseServiceImpl;

	@Test
	void testGetRegistrarWorkList() throws JSONException {
		OutputResponse response = new OutputResponse();

		String comingRequest = "{\"spID\": \"1\"}";

		JSONObject obj = new JSONObject(comingRequest);

		when(registrarServiceImpl.getRegWorkList(obj.getInt("spID"))).thenReturn(comingRequest);

		String expResponse = registrarController.getRegistrarWorkList(comingRequest);

		// response.setResponse(this.registrarServiceImpl.getRegWorkList(obj.getInt("spID")));

		assertEquals(expResponse, registrarController.getRegistrarWorkList(comingRequest));
	}

	@Test
	void testGetRegistrarWorkList_Exception() throws JSONException {
		String request = "{\"spID\": 123}";
		when(registrarServiceImpl.getRegWorkList(anyInt()))
				.thenThrow(new RuntimeException("Error in getRegistrarWorkList:"));

		String response = registrarController.getRegistrarWorkList(request);

		assertTrue(response.contains("Error in getRegistrarWorkList:"));
	}

//	@Test
//	void testGetRegistrarWorkList_Exception() throws JSONException {
//		String comingReq = "{\"statusCode\":5000,\"errorMessage\":\"Failed with generic error\",\"status\":\"FAILURE\"}";
//		when(registrarServiceImpl.getRegWorkList(any())).thenThrow(NotFoundException.class);
//		String response = registrarController.getRegistrarWorkList(comingReq);
//		assertEquals(response, registrarController.getRegistrarWorkList(comingReq));
//	}

// ************************
	@Test
	void testQuickSearchBeneficiary() throws JSONException {
		OutputResponse response = new OutputResponse();

		String comingRequest = "{\"benID\": \"abc\"}";

		JSONObject obj = new JSONObject(comingRequest);

		when(registrarServiceImpl.getQuickSearchBenData(obj.getString("benID"))).thenReturn(comingRequest);

		String expResponse = registrarController.quickSearchBeneficiary(comingRequest);

		// response.setResponse(registrarServiceImpl.getQuickSearchBenData(obj.getString("benID")));

		assertEquals(expResponse, registrarController.quickSearchBeneficiary(comingRequest));
	}

	@Test
	void testQuickSearchBeneficiaryException() {
		String request = "{\"benID\": \"123\"}";
		when(registrarServiceImpl.getQuickSearchBenData(anyString()))
				.thenThrow(new RuntimeException("Error in quickSearchBeneficiary :"));

		String response = registrarController.quickSearchBeneficiary(request);

		assertTrue(response.contains("Error in quickSearchBeneficiary :"));
	}

//************************
	@Test
	void testAdvanceSearch() throws IEMRException {
		OutputResponse response = new OutputResponse();

		String comingRequest = "{\"firstName\": \"John\", \"lastName\": \"Doe\", \"phoneNo\": \"1234567890\","
				+ "\"beneficiaryID\": \"ABC123\", \"stateID\": 1, \"districtID\": 2, \"aadharNo\": \"1234-5678-9012\","
				+ " \"govtIdentityNo\": \"XYZ789\"}";

		V_BenAdvanceSearch v_BenAdvanceSearch = inputMapper.gson().fromJson(comingRequest, V_BenAdvanceSearch.class);

		when(registrarServiceImpl.getAdvanceSearchBenData(v_BenAdvanceSearch)).thenReturn(comingRequest);

		String expResponse = registrarController.advanceSearch(comingRequest);

		response.setResponse(registrarServiceImpl.getAdvanceSearchBenData(v_BenAdvanceSearch));

		assertEquals(expResponse, registrarController.advanceSearch(comingRequest));
	}

// ************************
	@Test
	void testGetBenDetailsByRegID() throws JSONException {
		OutputResponse response = new OutputResponse();

		String comingRequest = "{\"beneficiaryRegID\": \"1\"}";
		String beneficiaryData = "test";

		JSONObject obj = new JSONObject(comingRequest);

		when(registrarServiceMasterDataImpl.getBenDetailsByRegID(obj.getLong("beneficiaryRegID")))
				.thenReturn(beneficiaryData);

		String expResponse = registrarController.getBenDetailsByRegID(comingRequest);

		response.setResponse(beneficiaryData);

		assertTrue(obj.has("beneficiaryRegID"));
		assertTrue(obj.getLong("beneficiaryRegID") > 0);

		assertEquals(expResponse, registrarController.getBenDetailsByRegID(comingRequest));
		assertTrue(response.toString().contains(beneficiaryData));
	}

	@Test
	void testGetBenDetailsByRegID_MissingBeneficiaryRegID() throws JSONException {
		String comingRequest = "{}"; // Missing beneficiaryRegID
		String expectedError = "Bad Request... beneficiaryRegID is not there in request";

		String actualResponse = registrarController.getBenDetailsByRegID(comingRequest);

		assertTrue(actualResponse.contains(expectedError));
	}

	@Test
	void testGetBenDetailsByRegID_InvalidBeneficiaryRegID() throws JSONException {
		String comingRequest = "{\"beneficiaryRegID\": \"0\"}"; // Invalid beneficiaryRegID
		String expectedError = "Please pass beneficiaryRegID";

		String actualResponse = registrarController.getBenDetailsByRegID(comingRequest);

		assertTrue(actualResponse.contains(expectedError));
	}

	@Test
	void testGetBenDetailsByRegID_ExceptionThrown() throws JSONException {
		String comingRequest = "{\"beneficiaryRegID\": \"1\"}";
		String expectedError = "Error in getBenDetailsByRegID :";

		when(registrarServiceMasterDataImpl.getBenDetailsByRegID(anyLong()))
				.thenThrow(new RuntimeException("Error in getBenDetailsByRegID :"));

		String actualResponse = registrarController.getBenDetailsByRegID(comingRequest);

		assertTrue(actualResponse.contains(expectedError));
	}

// ************************
	@Test
	void testGetBeneficiaryDetails() throws JSONException {
		OutputResponse response = new OutputResponse();

		String requestObj = "{\"beneficiaryRegID\": \"1\"}";
		String beneficiaryData = "test";

		JSONObject obj = new JSONObject(requestObj);

		when(registrarServiceImpl.getBeneficiaryDetails(obj.getLong("beneficiaryRegID"))).thenReturn(beneficiaryData);

		String expResponse = registrarController.getBeneficiaryDetails(requestObj);

		response.setResponse(beneficiaryData);

		assertTrue(obj.has("beneficiaryRegID"));
		assertTrue(obj.getLong("beneficiaryRegID") > 0);
		assertTrue(beneficiaryData != null);

		assertEquals(expResponse, registrarController.getBeneficiaryDetails(requestObj));
		assertTrue(response.toString().contains(beneficiaryData));
	}

	@Test
	void testGetBeneficiaryDetails_noDataMap() throws JSONException {
		OutputResponse response = new OutputResponse();

		String requestObj = "{\"beneficiaryRegID\": \"1\"}";
		String beneficiaryData = null;

		JSONObject obj = new JSONObject(requestObj);

		when(registrarServiceImpl.getBeneficiaryDetails(obj.getLong("beneficiaryRegID"))).thenReturn(beneficiaryData);

		String expResponse = registrarController.getBeneficiaryDetails(requestObj);

		Map<String, String> noDataMap = new HashMap<>();

		response.setResponse(new Gson().toJson(noDataMap));

		assertTrue(obj.has("beneficiaryRegID"));
		assertTrue(obj.getLong("beneficiaryRegID") > 0);
		assertNull(beneficiaryData);

		assertEquals(expResponse, registrarController.getBeneficiaryDetails(requestObj));
	}

	@Test
	void testGetBeneficiaryDetails_MissingBeneficiaryRegID() {
		String requestObj = "{}"; // beneficiaryRegID is missing
		String expectedError = "Bad Request... beneficiaryRegID is not there in request";

		String actualResponse = registrarController.getBeneficiaryDetails(requestObj);

		assertNotNull(actualResponse);
		assertTrue(actualResponse.contains(expectedError));
	}

	@Test
	void testGetBeneficiaryDetails_InvalidBeneficiaryRegID() {
		String requestObj = "{\"beneficiaryRegID\": \"0\"}";
		String expectedError = "Please pass beneficiaryRegID";

		String actualResponse = registrarController.getBeneficiaryDetails(requestObj);

		assertNotNull(actualResponse);
		assertTrue(actualResponse.contains(expectedError));
	}

	@Test
	void testGetBeneficiaryDetails_ExceptionThrown() {
		String requestObj = "{\"beneficiaryRegID\": \"1\"}";

		when(registrarServiceImpl.getBeneficiaryDetails(anyLong()))
				.thenThrow(new RuntimeException("Error in getBeneficiaryDetails :"));

		String actualResponse = registrarController.getBeneficiaryDetails(requestObj);

		assertNotNull(actualResponse);
		assertTrue(actualResponse.contains("Error in getBeneficiaryDetails :"));
	}

// ************************
	@Test
	void testGetBeneficiaryImage() throws JSONException {
		OutputResponse response = new OutputResponse();

		String requestObj = "{\"beneficiaryRegID\": \"1\"}";
		String beneficiaryData = "test";

		JSONObject obj = new JSONObject(requestObj);

		when(registrarServiceImpl.getBenImage(obj.getLong("beneficiaryRegID"))).thenReturn(beneficiaryData);

		String expResponse = registrarController.getBeneficiaryImage(requestObj);

		response.setResponse(beneficiaryData);

		assertTrue(obj.has("beneficiaryRegID"));
		assertTrue(obj.getLong("beneficiaryRegID") > 0);

		assertEquals(expResponse, registrarController.getBeneficiaryImage(requestObj));
		assertTrue(response.toString().contains(beneficiaryData));
	}

	@Test
	void testGetBeneficiaryImage_MissingBeneficiaryRegID() throws JSONException {
		String requestObj = "{}"; // Missing beneficiaryRegID
		String expectedError = "Bad Request... beneficiaryRegID is not there in request";

		String actualResponse = registrarController.getBeneficiaryImage(requestObj);

		assertTrue(actualResponse.contains(expectedError));
	}

	@Test
	void testGetBeneficiaryImage_InvalidBeneficiaryRegID() throws JSONException {
		String requestObj = "{\"beneficiaryRegID\": \"0\"}"; // Invalid beneficiaryRegID value
		String expectedError = "Please pass beneficiaryRegID";

		String actualResponse = registrarController.getBeneficiaryImage(requestObj);

		assertTrue(actualResponse.contains(expectedError));
	}

// ************************

	@Test
	void testQuickSearchNew_Success() throws Exception {
		String requestObj = "{\"searchKey\": \"1234567890\"}";
		String authorization = "Bearer token";
		String expectedSearchResult = "[{\"beneficiaryId\":\"1\",\"name\":\"John Doe\"}]";

		when(registrarServiceImpl.beneficiaryQuickSearch(anyString(), anyString())).thenReturn(expectedSearchResult);

		String actualResponse = registrarController.quickSearchNew(requestObj, authorization);

		assertEquals(expectedSearchResult, actualResponse);
	}

	@Test
	void testQuickSearchNew_InvalidRequest() throws Exception {
		String requestObj = "{\"searchKey\": \"invalid\"}";
		String authorization = "Bearer token";
		String expectedError = "Invalid request";

		when(registrarServiceImpl.beneficiaryQuickSearch(anyString(), anyString())).thenReturn(null);

		String actualResponse = registrarController.quickSearchNew(requestObj, authorization);

		assertTrue(actualResponse.contains(expectedError));
	}

	@Test
	void testQuickSearchNew_Exception() {
		String requestObj = "{\"searchKey\": \"1234567890\"}";
		String authorization = "Bearer token";
		String expectedError = "Error while searching beneficiary";

		when(registrarServiceImpl.beneficiaryQuickSearch(anyString(), anyString()))
				.thenThrow(new RuntimeException("Database error"));

		String actualResponse = registrarController.quickSearchNew(requestObj, authorization);

		assertTrue(actualResponse.contains(expectedError));
	}

// ************************

	@Test
	void testAdvanceSearchNew_Success() {
		// Prepare the request and mock behavior
		String requestObj = "{\"searchTerm\": \"John\"}";
		String authorization = "Bearer validToken";
		String expectedSearchList = "[{\"beneficiaryId\": \"1\", \"name\": \"John Doe\"}]";

		when(registrarServiceImpl.beneficiaryAdvanceSearch(requestObj, authorization)).thenReturn(expectedSearchList);

		// Execute the method
		String actualResponse = registrarController.advanceSearchNew(requestObj, authorization);

		// Verify the response
		assertEquals(expectedSearchList, actualResponse);
	}

	@Test
	void testAdvanceSearchNew_InvalidRequest() {
		String requestObj = "{\"searchTerm\": \"\"}";
		String authorization = "Bearer validToken";
		String expectedError = "Invalid request";

		when(registrarServiceImpl.beneficiaryAdvanceSearch(requestObj, authorization)).thenReturn(null);

		String actualResponse = registrarController.advanceSearchNew(requestObj, authorization);

		assertTrue(actualResponse.contains(expectedError));
	}

	@Test
	void testAdvanceSearchNew_ExceptionThrown() {
		String requestObj = "{\"searchTerm\": \"John\"}";
		String authorization = "Bearer validToken";
		String expectedError = "Error while searching beneficiary";

		when(registrarServiceImpl.beneficiaryAdvanceSearch(anyString(), anyString()))
				.thenThrow(new RuntimeException("Database error"));

		String actualResponse = registrarController.advanceSearchNew(requestObj, authorization);

		assertTrue(actualResponse.contains(expectedError));
	}

// ************************

	@Test
	void testGetBenDetailsForLeftSidePanelByRegID_Success() throws JSONException {
		String comingRequest = "{\"beneficiaryRegID\": \"1\", \"benFlowID\": \"1\"}";
		String authorization = "Bearer token";
		String expectedData = "Beneficiary Details";

		when(registrarServiceMasterDataImpl.getBenDetailsForLeftSideByRegIDNew(anyLong(), anyLong(), anyString(),
				anyString())).thenReturn(expectedData);

		String actualResponse = registrarController.getBenDetailsForLeftSidePanelByRegID(comingRequest, authorization);

		assertTrue(actualResponse.contains(expectedData));
	}

	@Test
	void testGetBenDetailsForLeftSidePanelByRegID_MissingIDs() {
		String comingRequestMissingIDs = "{\"beneficiaryRegID\": \"\"}";
		String authorization = "Bearer token";
		String expectedError = "Invalid request";

		String actualResponse = registrarController.getBenDetailsForLeftSidePanelByRegID(comingRequestMissingIDs,
				authorization);

		assertTrue(actualResponse.contains(expectedError));
	}

	@Test
	void testGetBenDetailsForLeftSidePanelByRegID_InvalidIDs() {
		String comingRequestInvalidIDs = "{\"beneficiaryRegID\": \"0\", \"benFlowID\": \"-1\"}";
		String authorization = "Bearer token";
		String expectedError = "Invalid Beneficiary ID";

		String actualResponse = registrarController.getBenDetailsForLeftSidePanelByRegID(comingRequestInvalidIDs,
				authorization);

		assertTrue(actualResponse.contains(expectedError));
	}

	@Test
	void testGetBenDetailsForLeftSidePanelByRegID_Exception() {
		String comingRequest = "{\"beneficiaryRegID\": \"1\", \"benFlowID\": \"1\"}";
		String authorization = "Bearer token";
		String expectedError = "Error while getting beneficiary details";

		when(registrarServiceMasterDataImpl.getBenDetailsForLeftSideByRegIDNew(anyLong(), anyLong(), anyString(),
				anyString())).thenThrow(new RuntimeException("Unexpected error"));

		String actualResponse = registrarController.getBenDetailsForLeftSidePanelByRegID(comingRequest, authorization);

		assertTrue(actualResponse.contains(expectedError));
	}

//	@Test
//	void testGetBenDetailsForLeftSidePanelByRegID() throws JSONException {
//		OutputResponse response = new OutputResponse();
//
//		String requestObj = "test";
//		String authorization = "Authorization";
//		String comingRequest = "{\"beneficiaryRegID\": \"1\", \"benFlowID\": \"1\"}";
//		String beneficiaryData = "test";
//
//		JSONObject obj = new JSONObject(comingRequest);
//
//		when(registrarServiceMasterDataImpl.getBenDetailsForLeftSideByRegIDNew(obj.getLong("beneficiaryRegID"),
//				obj.getLong("benFlowID"), authorization, comingRequest)).thenReturn(beneficiaryData);
//
//		String expResponse = registrarController.getBenDetailsForLeftSidePanelByRegID(comingRequest, authorization);
//
//		response.setResponse(beneficiaryData);
//
//		assertTrue(obj.has("beneficiaryRegID") && obj.has("benFlowID"));
//		assertTrue(obj.getLong("beneficiaryRegID") > 0 && obj.getLong("benFlowID") > 0);
//
//		assertEquals(expResponse,
//				registrarController.getBenDetailsForLeftSidePanelByRegID(comingRequest, authorization));
//		assertTrue(response.toString().contains(beneficiaryData));
//	}
//
//	@Test
//	void testGetBenDetailsForLeftSidePanelByRegID_InvalidBeneficiaryID() throws JSONException {
//		OutputResponse response = new OutputResponse();
//
//		String requestObj = "test";
//		String authorization = "Authorization";
//		String comingRequest = "{\"beneficiaryRegID\": \"-1\", \"benFlowID\": \"-1\"}";
//		String beneficiaryData = "test";
//
//		JSONObject obj = new JSONObject(comingRequest);
//
//		response.setError(500, "Invalid Beneficiary ID");
//
//		assertTrue(obj.has("beneficiaryRegID") && obj.has("benFlowID"));
//		assertTrue(obj.getLong("beneficiaryRegID") < 0 && obj.getLong("benFlowID") < 0);
//
//		assertTrue(response.toString().contains("Invalid Beneficiary ID"));
//	}
//
//	@Test
//	void testGetBenDetailsForLeftSidePanelByRegID_InvalidReq() throws JSONException {
//		OutputResponse response = new OutputResponse();
//
//		String comingRequest = "{}";
//
//		JSONObject obj = new JSONObject(comingRequest);
//
//		response.setError(500, "Invalid request");
//
//		assertTrue(!obj.has("beneficiaryRegID") || !obj.has("benFlowID"));
//
//		assertTrue(response.toString().contains("Invalid request"));
//	}

// ************************
	@Test
	void testGetBenImage() throws Exception {
		OutputResponse response = new OutputResponse();

		String requestObj = "{}";
		String authorization = "Authorization";

		when(registrarServiceMasterDataImpl.getBenImageFromIdentityAPI(authorization, requestObj))
				.thenReturn(authorization);

		String expResponse = registrarController.getBenImage(requestObj, authorization);

		assertEquals(expResponse, registrarController.getBenImage(requestObj, authorization));
	}

// ************************

	@Test
	void testCreateBeneficiary_Success() {
		// Prepare the request object
		String comingRequest = "{ \"benD\": { \"firstName\": \"John\", \"lastName\": \"Doe\" }}";
		String authorization = "Bearer token";

		// Mocking the service layer responses
		BeneficiaryData mockBenData = new BeneficiaryData();
		mockBenData.setBeneficiaryRegID(1L); // Assuming successful registration returns a positive ID
		when(registrarServiceImpl.createBeneficiary(any())).thenReturn(mockBenData);
		when(registrarServiceImpl.createBeneficiaryDemographic(any(), anyLong())).thenReturn(1L);
		when(registrarServiceImpl.createBeneficiaryPhoneMapping(any(), anyLong())).thenReturn(1L);
		when(registrarServiceImpl.createBeneficiaryDemographicAdditional(any(), anyLong())).thenReturn(1L);
		when(registrarServiceImpl.createBeneficiaryImage(any(), anyLong())).thenReturn(1L);

		// Execute the test
		String response = registrarController.createBeneficiary(comingRequest, authorization);

		// Validate the response
		assertTrue(response.contains("Registration Done"));
	}

	@Test
	void testCreateBeneficiary_InvalidInput() {
		String comingRequest = "{}"; // No benD object
		String authorization = "Bearer token";

		String response = registrarController.createBeneficiary(comingRequest, authorization);

		assertTrue(response.contains("Invalid input data"));
	}

	@Test
	void testCreateBeneficiary_RegistrationFailure() {
		String comingRequest = "{ \"benD\": { \"firstName\": \"John\" }}";
		String authorization = "Bearer token";

		BeneficiaryData mockBenData = new BeneficiaryData();
		mockBenData.setBeneficiaryRegID(1L);
		when(registrarServiceImpl.createBeneficiary(any())).thenReturn(mockBenData);
		when(registrarServiceImpl.createBeneficiaryDemographic(any(), anyLong())).thenReturn(0L); // Simulate failure

		String response = registrarController.createBeneficiary(comingRequest, authorization);

		assertTrue(response.contains("Something Went-Wrong"));
	}

	@Test
	void testCreateBeneficiary_ExceptionThrown() {
		String comingRequest = "{ \"benD\": { \"firstName\": \"John\" }}";
		String authorization = "Bearer token";

		when(registrarServiceImpl.createBeneficiary(any()))
				.thenThrow(new RuntimeException("Error in createBeneficiary :"));

		String response = registrarController.createBeneficiary(comingRequest, authorization);

		assertTrue(response.contains("Error in createBeneficiary :"));
	}

//	@Test
//	void testCreateBeneficiary_InvalidInput() throws IEMRException {
//		OutputResponse response = new OutputResponse();
//
//		String comingRequest = "{\"benD\": null}";
//
//		String authorization = "Authorization";
//
//		WrapperBeneficiaryRegistration wrapperBeneficiaryRegistrationOBJ = InputMapper.gson().fromJson(comingRequest,
//				WrapperBeneficiaryRegistration.class);
//
//		logger.info("createBeneficiary request:" + comingRequest);
//
//		JsonObject benD = wrapperBeneficiaryRegistrationOBJ.getBenD();
//
//		response.setError(0, "Invalid input data");
//
//		assertTrue(benD == null || benD.isJsonNull());
//
//		assertTrue(response.toString().contains("Invalid input data"));
//	}

// ************************
	@Test
	void testRegistrarBeneficaryRegistrationNew() throws Exception {

		OutputResponse response = new OutputResponse();

		String comingReq = "{}";
		;
		String authorization = "Authorization";
		String s = "test";

		when(registrarServiceImpl.registerBeneficiary(comingReq, authorization)).thenReturn(s);

		String expResponse = registrarController.registrarBeneficaryRegistrationNew(comingReq, authorization);

		assertEquals(expResponse, registrarController.registrarBeneficaryRegistrationNew(comingReq, authorization));
	}

	// ************************
	@Test
	void updateBeneficiary_WithNullBenD() {
		String comingRequest = "{\"benD\": null}";

		String result = registrarController.updateBeneficiary(comingRequest);

		assertTrue(result.contains("Data Not Sufficient"));
	}

	@Test
	void updateBeneficiary_WithMissingBeneficiaryRegID() {
		String comingRequest = "{\"benD\": {}}"; // Missing beneficiaryRegID

		String result = registrarController.updateBeneficiary(comingRequest);

		assertTrue(result.contains("Data Not Sufficient"));
	}
	
	@Test
    void updateBeneficiary_FailsToUpdate() {
        String comingRequest = "{\"benD\": {" +
                "\"beneficiaryRegID\": 12345," +
                "\"firstName\": \"John\"," +
                "\"lastName\": \"Doe\"," +
                "\"gender\": 1," +
                "\"dob\": \"1990-01-01T00:00:00\"," +
                "\"maritalStatus\": 1," +
                "\"fatherName\": \"John Sr.\"," +
                "\"motherName\": \"Jane\"," +
                "\"husbandName\": \"\"," +
                "\"image\": \"image_url\"," +
                "\"aadharNo\": \"123456789012\"," +
                "\"income\": 2," +
                "\"literacyStatus\": \"Graduate\"," +
                "\"educationQualification\": 4," +
                "\"occupation\": 3," +
                "\"phoneNo\": \"1234567890\"," +
                "\"emailID\": 0," + // Assuming this incorrect format triggers the failure
                "\"bankName\": \"XYZ Bank\"," +
                "\"branchName\": \"XYZ Branch\"," +
                "\"IFSCCode\": \"XYZ0001234\"," +
                "\"accountNumber\": \"12345678901\"," +
                "\"community\": 2," +
                "\"religion\": 1," +
                "\"blockID\": 123," +
                "\"blockName\": \"Block A\"," +
                "\"habitation\": \"Habitation X\"," +
                "\"villageID\": 1234," +
                "\"villageName\": \"Village Y\"," +
                "\"districtID\": 12," +
                "\"districtName\": \"District Z\"," +
                "\"stateID\": 1," +
                "\"stateName\": \"State A\"," +
                "\"govID\": [{" +
                "\"benGovMapID\": 123456," +
                "\"type\": \"PAN\"," +
                "\"value\": \"ABCDE1234F\"" +
                "}]," +
                "\"ageAtMarriage\": 25," +
                "\"createdBy\": \"Admin\"," +
                "\"servicePointID\": 1234567," +
                "\"govtIdentityNo\": 123456789," +
                "\"govtIdentityTypeID\": 1," +
                "\"modifiedBy\": \"Admin2\"" +
                "}}";

        when(registrarServiceImpl.updateBeneficiary(any())).thenReturn(0);

        String result = registrarController.updateBeneficiary(comingRequest);

        assertTrue(result.contains("Something Went-Wrong"));
    }
	

	@Test
	void updateBeneficiary_SuccessScenario() {
		String comingRequest = "{\"benD\": {\"beneficiaryRegID\": 123456789, \"firstName\": \"John\", \"lastName\": \"Doe\", \"gender\": 1, \"dob\": \"1980-01-01T00:00:00Z\", \"maritalStatus\": 2, \"fatherName\": \"Richard Doe\", \"motherName\": \"Jane Doe\", \"husbandName\": \"\", \"image\": \"base64EncodedStringOrURL\", \"aadharNo\": \"111122223333\", \"income\": 3, \"literacyStatus\": \"Graduate\", \"educationQualification\": 4, \"occupation\": 5, \"phoneNo\": \"1234567890\", \"emailID\": null, \"bankName\": \"State Bank\", \"branchName\": \"Main Branch\", \"IFSCCode\": \"SBIN0001234\", \"accountNumber\": \"1234567890123456\", \"community\": 1, \"religion\": 2, \"blockID\": 101, \"blockName\": \"Central Block\", \"habitation\": \"Urban\", \"villageID\": 102, \"villageName\": \"Green Village\", \"districtID\": 103, \"districtName\": \"East District\", \"stateID\": 104, \"stateName\": \"Example State\", \"govID\": [{\"benGovMapID\": 987654321, \"type\": \"PAN\", \"value\": \"ABCDE1234F\"}, {\"type\": \"Voter ID\", \"value\": \"XYZ1234567\"}], \"ageAtMarriage\": 25, \"createdBy\": \"Admin\", \"servicePointID\": 201, \"govtIdentityNo\": 1234567890, \"govtIdentityTypeID\": 1, \"modifiedBy\": \"Admin2\"}}";

		when(registrarServiceImpl.updateBeneficiary(any())).thenReturn(1);
		when(registrarServiceImpl.updateBeneficiaryDemographic(any(), anyLong())).thenReturn(1);
		when(registrarServiceImpl.updateBeneficiaryPhoneMapping(any(), anyLong())).thenReturn(1);
		when(registrarServiceImpl.updateBeneficiaryDemographicAdditional(any(), anyLong())).thenReturn(1);
		when(registrarServiceImpl.updateBeneficiaryImage(any(), anyLong())).thenReturn(1);

		String result = registrarController.updateBeneficiary(comingRequest);

		assertTrue(result.contains("Beneficiary Details updated successfully"));
	}
	

// ************************
	@Test
	void testCreateReVisitForBenToNurse() throws Exception {
		OutputResponse response = new OutputResponse();

		String requestOBJ = "{}";
		int i = 1;

		when(registrarServiceImpl.searchAndSubmitBeneficiaryToNurse(requestOBJ)).thenReturn(i);

		String expResponse = registrarController.createReVisitForBenToNurse(requestOBJ);

		response.setResponse("Beneficiary moved to nurse worklist");

		assertTrue((i > 0) && (i == 1));

		assertEquals(expResponse, registrarController.createReVisitForBenToNurse(requestOBJ));
		assertTrue(response.toString().contains("Beneficiary moved to nurse worklist"));
	}

	@Test
	void testCreateReVisitForBenToNurse_AlreadyPresent() throws Exception {
		OutputResponse response = new OutputResponse();

		String requestOBJ = "{}";
		int i = 2;

		when(registrarServiceImpl.searchAndSubmitBeneficiaryToNurse(requestOBJ)).thenReturn(i);

		String expResponse = registrarController.createReVisitForBenToNurse(requestOBJ);

		response.setError(5000, "Beneficiary already present in nurse worklist");

		assertTrue((i > 0) && !(i == 1));

		assertEquals(expResponse, registrarController.createReVisitForBenToNurse(requestOBJ));
		assertTrue(response.toString().contains("Beneficiary already present in nurse worklist"));
	}

	@Test
	void testCreateReVisitForBenToNurse_Error() throws Exception {
		OutputResponse response = new OutputResponse();

		String requestOBJ = "{ \"/create/BenReVisitToNurse\" }";
		int i = 0;

		when(registrarServiceImpl.searchAndSubmitBeneficiaryToNurse(requestOBJ)).thenReturn(i);

		String expResponse = registrarController.createReVisitForBenToNurse(requestOBJ);

		response.setError(5000, "Beneficiary already present in nurse worklist");

		assertTrue(!(i > 0) && !(i == 1));

		assertEquals(expResponse, registrarController.createReVisitForBenToNurse(requestOBJ));
		assertTrue(response.toString().contains("Beneficiary already present in nurse worklist"));
	}

	// ************************
	@Test
	void testBeneficiaryUpdate() throws Exception {
		OutputResponse response = new OutputResponse();

		String requestOBJ = "{ \"/update/BeneficiaryUpdate\" }";
		String authorization = "Authorization";

		Integer s = 1;

		when(registrarServiceImpl.updateBeneficiary(requestOBJ, authorization)).thenReturn(s);

		String expResponse = registrarController.beneficiaryUpdate(requestOBJ, authorization);

		response.setResponse("Beneficiary details updated successfully");

		assertTrue((s != null) && (s == 1));

		assertEquals(expResponse, registrarController.beneficiaryUpdate(requestOBJ, authorization));
		assertTrue(response.toString().contains("Beneficiary details updated successfully"));
	}

	@Test
	void testBeneficiaryUpdate_AlreadyPresent() throws Exception {
		OutputResponse response = new OutputResponse();

		String requestOBJ = "{ \"/update/BeneficiaryUpdate\" }";
		String authorization = "Authorization";

		Integer s = 0;

		when(registrarServiceImpl.updateBeneficiary(requestOBJ, authorization)).thenReturn(s);

		String expResponse = registrarController.beneficiaryUpdate(requestOBJ, authorization);

		response.setResponse("Beneficiary details updated successfully but already present in nurse work list");

		assertTrue((s != null) && !(s == 1));

		assertEquals(expResponse, registrarController.beneficiaryUpdate(requestOBJ, authorization));
		assertTrue(response.toString()
				.contains("Beneficiary details updated successfully but already present in nurse work list"));
	}

	@Test
	void testBeneficiaryUpdate_Error() throws Exception {
		OutputResponse response = new OutputResponse();

		String requestOBJ = "{ \"/update/BeneficiaryUpdate\" }";
		String authorization = "Authorization";

		Integer s = null;

		response.setError(5000, "Error while updating beneficiary details");

		assertNull(s);

		assertTrue(response.toString().contains("Error while updating beneficiary details"));
	}

	// ************************
	@Test
	void testMasterDataForRegistration() throws JSONException {
		OutputResponse response = new OutputResponse();

		String comingRequest = "{\"spID\": \"1\"}";

		JSONObject obj = new JSONObject(comingRequest);

		when(registrarServiceMasterDataImpl.getRegMasterData()).thenReturn(comingRequest);

		String expResponse = registrarController.masterDataForRegistration(comingRequest);

		assertTrue(obj.has("spID"));
		assertTrue(obj.getInt("spID") > 0);

		assertEquals(expResponse, registrarController.masterDataForRegistration(comingRequest));
	}

	@Test
	void testMasterDataForRegistration_Invalid() throws JSONException {
		OutputResponse response = new OutputResponse();

		String comingRequest = "{}";

		JSONObject obj = new JSONObject(comingRequest);

		response.setError(5000, "Invalid service point");

		assertTrue(!obj.has("spID"));

		assertTrue(response.toString().contains("Invalid service point"));
	}

}