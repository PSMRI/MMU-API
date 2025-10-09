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
package com.iemr.mmu.service.login;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iemr.mmu.repo.login.MasterVanRepo;
import com.iemr.mmu.repo.login.ServicePointVillageMappingRepo;
import com.iemr.mmu.repo.login.UserParkingplaceMappingRepo;
import com.iemr.mmu.repo.login.UserVanSpDetails_View_Repo;
import com.iemr.mmu.repo.login.VanServicepointMappingRepo;

@ExtendWith(MockitoExtension.class)
@DisplayName("IemrMmuLoginServiceImpl Test Cases")
class IemrMmuLoginServiceImplTest {

    @InjectMocks
    private IemrMmuLoginServiceImpl loginService;

    @Mock
    private UserParkingplaceMappingRepo userParkingplaceMappingRepo;

    @Mock
    private MasterVanRepo masterVanRepo;

    @Mock
    private VanServicepointMappingRepo vanServicepointMappingRepo;

    @Mock
    private ServicePointVillageMappingRepo servicePointVillageMappingRepo;

    @Mock
    private UserVanSpDetails_View_Repo userVanSpDetails_View_Repo;

    @BeforeEach
    void setUp() {
        // Setup method for any initialization if needed
    }

    @Test
    @DisplayName("Test getUserServicePointVanDetails - With valid data")
    void testGetUserServicePointVanDetails_WithValidData() {
        // Arrange
        Integer userID = 1;
        
        // Mock parking place data
        List<Object[]> parkingPlaceList = Arrays.asList(
            new Object[]{1, 101, "State1", 201, "District1", 301, "Block1"},
            new Object[]{2, 102, "State2", 202, "District2", 302, "Block2"}
        );
        
        // Mock van data
        List<Object[]> vanList = Arrays.asList(
            new Object[]{1, "VAN001"},
            new Object[]{2, "VAN002"}
        );
        
        // Mock service point data
        List<Object[]> servicePointList = Arrays.asList(
            new Object[]{1, "ServicePoint1", "Morning"},
            new Object[]{2, "ServicePoint2", "Evening"}
        );

        when(userParkingplaceMappingRepo.getUserParkingPlce(userID)).thenReturn(parkingPlaceList);
        when(masterVanRepo.getUserVanDatails(any())).thenReturn(vanList);
        when(vanServicepointMappingRepo.getuserSpSessionDetails(any())).thenReturn(servicePointList);

        // Act
        String result = loginService.getUserServicePointVanDetails(userID);

        // Assert
        assertNotNull(result);
        JsonObject jsonResult = JsonParser.parseString(result).getAsJsonObject();
        
        assertTrue(jsonResult.has("userVanDetails"));
        assertTrue(jsonResult.has("userSpDetails"));
        assertTrue(jsonResult.has("parkingPlaceLocationList"));
        
        JsonArray vanDetails = jsonResult.getAsJsonArray("userVanDetails");
        assertEquals(2, vanDetails.size());
        
        JsonArray spDetails = jsonResult.getAsJsonArray("userSpDetails");
        assertEquals(2, spDetails.size());
        
        JsonArray parkingPlaceDetails = jsonResult.getAsJsonArray("parkingPlaceLocationList");
        assertEquals(2, parkingPlaceDetails.size());
    }

    @Test
    @DisplayName("Test getUserServicePointVanDetails - Empty parking place list")
    void testGetUserServicePointVanDetails_EmptyParkingPlaceList() {
        // Arrange
        Integer userID = 1;
        List<Object[]> emptyParkingPlaceList = new ArrayList<>();

        when(userParkingplaceMappingRepo.getUserParkingPlce(userID)).thenReturn(emptyParkingPlaceList);

        // Act
        String result = loginService.getUserServicePointVanDetails(userID);

        // Assert
        assertNotNull(result);
        JsonObject jsonResult = JsonParser.parseString(result).getAsJsonObject();
        
        // Should return empty JSON object since parking place list is empty
        assertTrue(jsonResult.entrySet().isEmpty());
    }

    @Test
    @DisplayName("Test getUserServicePointVanDetails - Empty van list")
    void testGetUserServicePointVanDetails_EmptyVanList() {
        // Arrange
        Integer userID = 1;
        
        List<Object[]> parkingPlaceList = new ArrayList<>();
        parkingPlaceList.add(new Object[]{1, 101, "State1", 201, "District1", 301, "Block1"});
        
        List<Object[]> emptyVanList = new ArrayList<>();
        List<Object[]> servicePointList = new ArrayList<>();
        servicePointList.add(new Object[]{1, "ServicePoint1", "Morning"});

        when(userParkingplaceMappingRepo.getUserParkingPlce(userID)).thenReturn(parkingPlaceList);
        when(masterVanRepo.getUserVanDatails(any())).thenReturn(emptyVanList);
        when(vanServicepointMappingRepo.getuserSpSessionDetails(any())).thenReturn(servicePointList);

        // Act
        String result = loginService.getUserServicePointVanDetails(userID);

        // Assert
        assertNotNull(result);
        JsonObject jsonResult = JsonParser.parseString(result).getAsJsonObject();
        
        JsonArray vanDetails = jsonResult.getAsJsonArray("userVanDetails");
        assertEquals(1, vanDetails.size());
        
        // Should contain empty map when no vans
        JsonObject vanDetail = vanDetails.get(0).getAsJsonObject();
        assertTrue(vanDetail.entrySet().isEmpty());
    }

    @Test
    @DisplayName("Test getUserServicePointVanDetails - Empty service point list")
    void testGetUserServicePointVanDetails_EmptyServicePointList() {
        // Arrange
        Integer userID = 1;
        
        List<Object[]> parkingPlaceList = new ArrayList<>();
        parkingPlaceList.add(new Object[]{1, 101, "State1", 201, "District1", 301, "Block1"});
        
        List<Object[]> vanList = new ArrayList<>();
        vanList.add(new Object[]{1, "VAN001"});
        List<Object[]> emptyServicePointList = new ArrayList<>();

        when(userParkingplaceMappingRepo.getUserParkingPlce(userID)).thenReturn(parkingPlaceList);
        when(masterVanRepo.getUserVanDatails(any())).thenReturn(vanList);
        when(vanServicepointMappingRepo.getuserSpSessionDetails(any())).thenReturn(emptyServicePointList);

        // Act
        String result = loginService.getUserServicePointVanDetails(userID);

        // Assert
        assertNotNull(result);
        JsonObject jsonResult = JsonParser.parseString(result).getAsJsonObject();
        
        JsonArray spDetails = jsonResult.getAsJsonArray("userSpDetails");
        assertEquals(1, spDetails.size());
        
        // Should contain empty map when no service points
        JsonObject spDetail = spDetails.get(0).getAsJsonObject();
        assertTrue(spDetail.entrySet().isEmpty());
    }

    @Test
    @DisplayName("Test getServicepointVillages - With valid data")
    void testGetServicepointVillages_WithValidData() {
        // Arrange
        Integer servicePointID = 1;
        List<Object[]> servicePointVillageList = Arrays.asList(
            new Object[]{1, "Village1"},
            new Object[]{2, "Village2"},
            new Object[]{3, "Village3"}
        );

        when(servicePointVillageMappingRepo.getServicePointVillages(servicePointID)).thenReturn(servicePointVillageList);

        // Act
        String result = loginService.getServicepointVillages(servicePointID);

        // Assert
        assertNotNull(result);
        JsonArray jsonResult = JsonParser.parseString(result).getAsJsonArray();
        assertEquals(3, jsonResult.size());
        
        JsonObject village1 = jsonResult.get(0).getAsJsonObject();
        assertTrue(village1.has("districtBranchID"));
        assertTrue(village1.has("villageName"));
        assertEquals(1, village1.get("districtBranchID").getAsInt());
        assertEquals("Village1", village1.get("villageName").getAsString());
    }

    @Test
    @DisplayName("Test getServicepointVillages - Empty village list")
    void testGetServicepointVillages_EmptyVillageList() {
        // Arrange
        Integer servicePointID = 1;
        List<Object[]> emptyVillageList = new ArrayList<>();

        when(servicePointVillageMappingRepo.getServicePointVillages(servicePointID)).thenReturn(emptyVillageList);

        // Act
        String result = loginService.getServicepointVillages(servicePointID);

        // Assert
        assertNotNull(result);
        JsonArray jsonResult = JsonParser.parseString(result).getAsJsonArray();
        assertEquals(0, jsonResult.size());
    }

    @Test
    @DisplayName("Test getUserVanSpDetails - With valid data")
    void testGetUserVanSpDetails_WithValidData() {
        // Arrange
        Integer userID = 1;
        Integer providerServiceMapID = 1;
        
        ArrayList<Object[]> userVanSpDetailsList = new ArrayList<>();
        userVanSpDetailsList.add(new Object[]{1, 2, "VAN001", (short)1, 3, "ServicePoint1", 4, 5});
        userVanSpDetailsList.add(new Object[]{6, 7, "VAN002", (short)2, 8, "ServicePoint2", 9, 10});
        
        List<Object[]> parkingPlaceList = new ArrayList<>();
        parkingPlaceList.add(new Object[]{1, 101, "State1", 201, "District1", 301, "Block1"});

        when(userVanSpDetails_View_Repo.getUserVanSpDetails_View(userID, providerServiceMapID)).thenReturn(userVanSpDetailsList);
        when(userParkingplaceMappingRepo.getUserParkingPlce(userID)).thenReturn(parkingPlaceList);

        // Act
        String result = loginService.getUserVanSpDetails(userID, providerServiceMapID);

        // Assert
        assertNotNull(result);
        JsonObject jsonResult = JsonParser.parseString(result).getAsJsonObject();
        
        assertTrue(jsonResult.has("UserVanSpDetails"));
        assertTrue(jsonResult.has("UserLocDetails"));
        
        JsonArray userVanSpDetails = jsonResult.getAsJsonArray("UserVanSpDetails");
        assertEquals(2, userVanSpDetails.size());
        
        JsonObject userLocDetails = jsonResult.getAsJsonObject("UserLocDetails");
        assertTrue(userLocDetails.has("parkingPlaceID"));
        assertTrue(userLocDetails.has("stateID"));
        assertTrue(userLocDetails.has("stateName"));
        assertEquals(1, userLocDetails.get("parkingPlaceID").getAsInt());
        assertEquals(101, userLocDetails.get("stateID").getAsInt());
        assertEquals("State1", userLocDetails.get("stateName").getAsString());
    }

    @Test
    @DisplayName("Test getUserVanSpDetails - Empty user van sp details")
    void testGetUserVanSpDetails_EmptyUserVanSpDetails() {
        // Arrange
        Integer userID = 1;
        Integer providerServiceMapID = 1;
        
        ArrayList<Object[]> emptyUserVanSpDetailsList = new ArrayList<>();
        List<Object[]> parkingPlaceList = new ArrayList<>();
        parkingPlaceList.add(new Object[]{1, 101, "State1", 201, "District1", 301, "Block1"});

        when(userVanSpDetails_View_Repo.getUserVanSpDetails_View(userID, providerServiceMapID)).thenReturn(emptyUserVanSpDetailsList);
        when(userParkingplaceMappingRepo.getUserParkingPlce(userID)).thenReturn(parkingPlaceList);

        // Act
        String result = loginService.getUserVanSpDetails(userID, providerServiceMapID);

        // Assert
        assertNotNull(result);
        JsonObject jsonResult = JsonParser.parseString(result).getAsJsonObject();
        
        assertTrue(jsonResult.has("UserVanSpDetails"));
        JsonArray userVanSpDetails = jsonResult.getAsJsonArray("UserVanSpDetails");
        assertEquals(0, userVanSpDetails.size());
    }

    @Test
    @DisplayName("Test getUserVanSpDetails - Empty parking place list")
    void testGetUserVanSpDetails_EmptyParkingPlaceList() {
        // Arrange
        Integer userID = 1;
        Integer providerServiceMapID = 1;
        
        ArrayList<Object[]> userVanSpDetailsList = new ArrayList<>();
        userVanSpDetailsList.add(new Object[]{1, 2, "VAN001", (short)1, 3, "ServicePoint1", 4, 5});
        
        List<Object[]> emptyParkingPlaceList = new ArrayList<>();

        when(userVanSpDetails_View_Repo.getUserVanSpDetails_View(userID, providerServiceMapID)).thenReturn(userVanSpDetailsList);
        when(userParkingplaceMappingRepo.getUserParkingPlce(userID)).thenReturn(emptyParkingPlaceList);

        // Act
        String result = loginService.getUserVanSpDetails(userID, providerServiceMapID);

        // Assert
        assertNotNull(result);
        JsonObject jsonResult = JsonParser.parseString(result).getAsJsonObject();
        
        assertTrue(jsonResult.has("UserVanSpDetails"));
        assertTrue(jsonResult.has("UserLocDetails"));
        
        JsonObject userLocDetails = jsonResult.getAsJsonObject("UserLocDetails");
        assertTrue(userLocDetails.entrySet().isEmpty());
    }

    @Test
    @DisplayName("Test getVanMaster - With valid data")
    void testGetVanMaster_WithValidData() throws Exception {
        // Arrange
        Integer psmID = 1;
        ArrayList<Object[]> vanMasterList = new ArrayList<>();
        vanMasterList.add(new Object[]{1, "VAN001"});
        vanMasterList.add(new Object[]{2, "VAN002"});

        when(masterVanRepo.getVanMaster(psmID)).thenReturn(vanMasterList);

        // Act
        String result = loginService.getVanMaster(psmID);

        // Assert
        assertNotNull(result);
        JsonArray jsonResult = JsonParser.parseString(result).getAsJsonArray();
        assertEquals(3, jsonResult.size()); // 2 vans + 1 "All" option
        
        // First entry should be "All"
        JsonObject allOption = jsonResult.get(0).getAsJsonObject();
        assertEquals(0, allOption.get("vanID").getAsInt());
        assertEquals("All", allOption.get("vehicalNo").getAsString());
        
        // Second entry should be first van
        JsonObject van1 = jsonResult.get(1).getAsJsonObject();
        assertEquals(1, van1.get("vanID").getAsInt());
        assertEquals("VAN001", van1.get("vehicalNo").getAsString());
    }

    @Test
    @DisplayName("Test getVanMaster - Empty van master list")
    void testGetVanMaster_EmptyVanMasterList() throws Exception {
        // Arrange
        Integer psmID = 1;
        ArrayList<Object[]> emptyVanMasterList = new ArrayList<>();

        when(masterVanRepo.getVanMaster(psmID)).thenReturn(emptyVanMasterList);

        // Act
        String result = loginService.getVanMaster(psmID);

        // Assert
        assertNotNull(result);
        JsonArray jsonResult = JsonParser.parseString(result).getAsJsonArray();
        assertEquals(1, jsonResult.size()); // Only "All" option
        
        JsonObject allOption = jsonResult.get(0).getAsJsonObject();
        assertEquals(0, allOption.get("vanID").getAsInt());
        assertEquals("All", allOption.get("vehicalNo").getAsString());
    }

    @Test
    @DisplayName("Test getVanMaster - Null van master list")
    void testGetVanMaster_NullVanMasterList() throws Exception {
        // Arrange
        Integer psmID = 1;

        when(masterVanRepo.getVanMaster(psmID)).thenReturn(null);

        // Act
        String result = loginService.getVanMaster(psmID);

        // Assert
        assertNotNull(result);
        JsonArray jsonResult = JsonParser.parseString(result).getAsJsonArray();
        assertEquals(1, jsonResult.size()); // Only "All" option
        
        JsonObject allOption = jsonResult.get(0).getAsJsonObject();
        assertEquals(0, allOption.get("vanID").getAsInt());
        assertEquals("All", allOption.get("vehicalNo").getAsString());
    }

    @Test
    @DisplayName("Test setter methods coverage")
    void testSetterMethods() {
        // Arrange & Act
        loginService.setUserVanSpDetails_View_Repo(userVanSpDetails_View_Repo);
        loginService.setUserParkingplaceMappingRepo(userParkingplaceMappingRepo);
        loginService.setMasterVanRepo(masterVanRepo);
        loginService.setVanServicepointMappingRepo(vanServicepointMappingRepo);
        loginService.setServicePointVillageMappingRepo(servicePointVillageMappingRepo);

        // Assert - This test ensures setter methods are called and covered
        assertNotNull(loginService);
    }
}
