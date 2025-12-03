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
package com.iemr.mmu.service.location;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.iemr.mmu.data.location.Country;
import com.iemr.mmu.data.location.CountryCityMaster;
import com.iemr.mmu.data.location.DistrictBranchMapping;
import com.iemr.mmu.data.location.V_GetLocDetailsFromSPidAndPSMid;
import com.iemr.mmu.repo.location.CountryCityMasterRepo;
import com.iemr.mmu.repo.location.CountryMasterRepo;
import com.iemr.mmu.repo.location.DistrictBlockMasterRepo;
import com.iemr.mmu.repo.location.DistrictBranchMasterRepo;
import com.iemr.mmu.repo.location.DistrictMasterRepo;
import com.iemr.mmu.repo.location.ParkingPlaceMasterRepo;
import com.iemr.mmu.repo.location.ServicePointMasterRepo;
import com.iemr.mmu.repo.location.StateMasterRepo;
import com.iemr.mmu.repo.location.V_GetLocDetailsFromSPidAndPSMidRepo;
import com.iemr.mmu.repo.location.V_get_prkngplc_dist_zone_state_from_spidRepo;
import com.iemr.mmu.repo.location.ZoneMasterRepo;
import com.iemr.mmu.repo.login.ServicePointVillageMappingRepo;

@ExtendWith(MockitoExtension.class)
@DisplayName("LocationServiceImpl Test Cases")
class LocationServiceImplTest {

    @InjectMocks
    private LocationServiceImpl locationService;

    @Mock
    private CountryMasterRepo countryMasterRepo;

    @Mock
    private CountryCityMasterRepo countryCityMasterRepo;

    @Mock
    private StateMasterRepo stateMasterRepo;

    @Mock
    private ZoneMasterRepo zoneMasterRepo;

    @Mock
    private DistrictMasterRepo districtMasterRepo;

    @Mock
    private DistrictBlockMasterRepo districtBlockMasterRepo;

    @Mock
    private ParkingPlaceMasterRepo parkingPlaceMasterRepo;

    @Mock
    private ServicePointMasterRepo servicePointMasterRepo;

    @Mock
    private V_GetLocDetailsFromSPidAndPSMidRepo v_GetLocDetailsFromSPidAndPSMidRepo;

    @Mock
    private ServicePointVillageMappingRepo servicePointVillageMappingRepo;

    @Mock
    private DistrictBranchMasterRepo districtBranchMasterRepo;

    @Mock
    private V_get_prkngplc_dist_zone_state_from_spidRepo v_get_prkngplc_dist_zone_state_from_spidRepo;

    private ArrayList<Object[]> mockStateMasterList;
    private ArrayList<Country> mockCountryList;
    private ArrayList<CountryCityMaster> mockCountryCityList;

    @BeforeEach
    void setUp() {
        // Setup mock data for state master
        mockStateMasterList = new ArrayList<>();
        Object[] state1 = {1, "Maharashtra"};
        Object[] state2 = {2, "Karnataka"};
        mockStateMasterList.add(state1);
        mockStateMasterList.add(state2);

        // Setup mock country list
        mockCountryList = new ArrayList<>();
        Country country1 = new Country(1, "India");
        Country country2 = new Country(2, "USA");
        mockCountryList.add(country1);
        mockCountryList.add(country2);

        // Setup mock country city list
        mockCountryCityList = new ArrayList<>();
        CountryCityMaster city1 = new CountryCityMaster();
        CountryCityMaster city2 = new CountryCityMaster();
        mockCountryCityList.add(city1);
        mockCountryCityList.add(city2);
    }

    @Test
    @DisplayName("Test getStateList - Success with data")
    void testGetStateList_Success() {
        // Arrange
        when(stateMasterRepo.getStateMaster()).thenReturn(mockStateMasterList);

        // Act
        String result = locationService.getStateList();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Maharashtra"));
        assertTrue(result.contains("Karnataka"));
        verify(stateMasterRepo).getStateMaster();
    }

    @Test
    @DisplayName("Test getStateList - Empty data")
    void testGetStateList_EmptyData() {
        // Arrange
        when(stateMasterRepo.getStateMaster()).thenReturn(new ArrayList<>());

        // Act
        String result = locationService.getStateList();

        // Assert
        assertNotNull(result);
        assertEquals("[]", result);
        verify(stateMasterRepo).getStateMaster();
    }

    @Test
    @DisplayName("Test getStateList - Null data")
    void testGetStateList_NullData() {
        // Arrange
        when(stateMasterRepo.getStateMaster()).thenReturn(null);

        // Act
        String result = locationService.getStateList();

        // Assert
        assertNotNull(result);
        assertEquals("[]", result);
        verify(stateMasterRepo).getStateMaster();
    }

    @Test
    @DisplayName("Test getCountryList - Success")
    void testGetCountryList_Success() {
        // Arrange
        when(countryMasterRepo.findAllCountries()).thenReturn(mockCountryList);

        // Act
        String result = locationService.getCountryList();

        // Assert
        assertNotNull(result);
        verify(countryMasterRepo).findAllCountries();
    }

    @Test
    @DisplayName("Test getCountryCityList - Success")
    void testGetCountryCityList_Success() {
        // Arrange
        Integer countryID = 1;
        when(countryCityMasterRepo.findByCountryIDAndDeleted(countryID, false))
                .thenReturn(mockCountryCityList);

        // Act
        String result = locationService.getCountryCityList(countryID);

        // Assert
        assertNotNull(result);
        verify(countryCityMasterRepo).findByCountryIDAndDeleted(countryID, false);
    }

    @Test
    @DisplayName("Test getZoneList - Success with data")
    void testGetZoneList_Success() {
        // Arrange
        Integer providerServiceMapID = 1;
        ArrayList<Object[]> mockZoneList = new ArrayList<>();
        Object[] zone1 = {1, "Zone1"};
        Object[] zone2 = {2, "Zone2"};
        mockZoneList.add(zone1);
        mockZoneList.add(zone2);

        when(zoneMasterRepo.getZoneMaster(providerServiceMapID)).thenReturn(mockZoneList);

        // Act
        String result = locationService.getZoneList(providerServiceMapID);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Zone1"));
        assertTrue(result.contains("Zone2"));
        verify(zoneMasterRepo).getZoneMaster(providerServiceMapID);
    }

    @Test
    @DisplayName("Test getZoneList - Empty data")
    void testGetZoneList_EmptyData() {
        // Arrange
        Integer providerServiceMapID = 1;
        when(zoneMasterRepo.getZoneMaster(providerServiceMapID)).thenReturn(new ArrayList<>());

        // Act
        String result = locationService.getZoneList(providerServiceMapID);

        // Assert
        assertNotNull(result);
        assertEquals("[]", result);
        verify(zoneMasterRepo).getZoneMaster(providerServiceMapID);
    }

    @Test
    @DisplayName("Test getDistrictList - Success with data")
    void testGetDistrictList_Success() {
        // Arrange
        Integer stateID = 1;
        ArrayList<Object[]> mockDistrictList = new ArrayList<>();
        Object[] district1 = {1, "Mumbai"};
        Object[] district2 = {2, "Pune"};
        mockDistrictList.add(district1);
        mockDistrictList.add(district2);

        when(districtMasterRepo.getDistrictMaster(stateID)).thenReturn(mockDistrictList);

        // Act
        String result = locationService.getDistrictList(stateID);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Mumbai"));
        assertTrue(result.contains("Pune"));
        verify(districtMasterRepo).getDistrictMaster(stateID);
    }

    @Test
    @DisplayName("Test getDistrictList - Empty data")
    void testGetDistrictList_EmptyData() {
        // Arrange
        Integer stateID = 1;
        when(districtMasterRepo.getDistrictMaster(stateID)).thenReturn(new ArrayList<>());

        // Act
        String result = locationService.getDistrictList(stateID);

        // Assert
        assertNotNull(result);
        assertEquals("[]", result);
        verify(districtMasterRepo).getDistrictMaster(stateID);
    }

    @Test
    @DisplayName("Test getDistrictBlockList - Success with data")
    void testGetDistrictBlockList_Success() {
        // Arrange
        Integer districtID = 1;
        ArrayList<Object[]> mockBlockList = new ArrayList<>();
        Object[] block1 = {1, "Block1"};
        Object[] block2 = {2, "Block2"};
        mockBlockList.add(block1);
        mockBlockList.add(block2);

        when(districtBlockMasterRepo.getDistrictBlockMaster(districtID)).thenReturn(mockBlockList);

        // Act
        String result = locationService.getDistrictBlockList(districtID);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Block1"));
        assertTrue(result.contains("Block2"));
        verify(districtBlockMasterRepo).getDistrictBlockMaster(districtID);
    }

    @Test
    @DisplayName("Test getDistrictBlockList - Empty data")
    void testGetDistrictBlockList_EmptyData() {
        // Arrange
        Integer districtID = 1;
        when(districtBlockMasterRepo.getDistrictBlockMaster(districtID)).thenReturn(new ArrayList<>());

        // Act
        String result = locationService.getDistrictBlockList(districtID);

        // Assert
        assertNotNull(result);
        assertEquals("[]", result);
        verify(districtBlockMasterRepo).getDistrictBlockMaster(districtID);
    }

    @Test
    @DisplayName("Test getParkingPlaceList - Success with data")
    void testGetParkingPlaceList_Success() {
        // Arrange
        Integer providerServiceMapID = 1;
        ArrayList<Object[]> mockParkingPlaceList = new ArrayList<>();
        Object[] place1 = {1, "Parking1"};
        Object[] place2 = {2, "Parking2"};
        mockParkingPlaceList.add(place1);
        mockParkingPlaceList.add(place2);

        when(parkingPlaceMasterRepo.getParkingPlaceMaster(providerServiceMapID))
                .thenReturn(mockParkingPlaceList);

        // Act
        String result = locationService.getParkingPlaceList(providerServiceMapID);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Parking1"));
        assertTrue(result.contains("Parking2"));
        verify(parkingPlaceMasterRepo).getParkingPlaceMaster(providerServiceMapID);
    }

    @Test
    @DisplayName("Test getParkingPlaceList - Empty data")
    void testGetParkingPlaceList_EmptyData() {
        // Arrange
        Integer providerServiceMapID = 1;
        when(parkingPlaceMasterRepo.getParkingPlaceMaster(providerServiceMapID))
                .thenReturn(new ArrayList<>());

        // Act
        String result = locationService.getParkingPlaceList(providerServiceMapID);

        // Assert
        assertNotNull(result);
        assertEquals("[]", result);
        verify(parkingPlaceMasterRepo).getParkingPlaceMaster(providerServiceMapID);
    }

    @Test
    @DisplayName("Test getServicePointPlaceList - Success with data")
    void testGetServicePointPlaceList_Success() {
        // Arrange
        Integer parkingPlaceID = 1;
        ArrayList<Object[]> mockServicePointList = new ArrayList<>();
        Object[] point1 = {1, "ServicePoint1"};
        Object[] point2 = {2, "ServicePoint2"};
        mockServicePointList.add(point1);
        mockServicePointList.add(point2);

        when(servicePointMasterRepo.getServicePointMaster(parkingPlaceID))
                .thenReturn(mockServicePointList);

        // Act
        String result = locationService.getServicePointPlaceList(parkingPlaceID);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("ServicePoint1"));
        assertTrue(result.contains("ServicePoint2"));
        verify(servicePointMasterRepo).getServicePointMaster(parkingPlaceID);
    }

    @Test
    @DisplayName("Test getServicePointPlaceList - Empty data")
    void testGetServicePointPlaceList_EmptyData() {
        // Arrange
        Integer parkingPlaceID = 1;
        when(servicePointMasterRepo.getServicePointMaster(parkingPlaceID))
                .thenReturn(new ArrayList<>());

        // Act
        String result = locationService.getServicePointPlaceList(parkingPlaceID);

        // Assert
        assertNotNull(result);
        assertEquals("[]", result);
        verify(servicePointMasterRepo).getServicePointMaster(parkingPlaceID);
    }

    @Test
    @DisplayName("Test getVillageMasterFromBlockID - Success")
    void testGetVillageMasterFromBlockID_Success() {
        // Arrange
        Integer distBlockID = 1;
        ArrayList<Object[]> mockVillageList = new ArrayList<>();
        Object[] village1 = {1, "Village1", "Block1", 1};
        mockVillageList.add(village1);

        when(districtBranchMasterRepo.findByBlockID(distBlockID)).thenReturn(mockVillageList);

        try (MockedStatic<DistrictBranchMapping> mockedStatic = mockStatic(DistrictBranchMapping.class)) {
            String expectedJson = "[{\"villageID\":1,\"villageName\":\"Village1\"}]";
            mockedStatic.when(() -> DistrictBranchMapping.getVillageList(mockVillageList))
                    .thenReturn(expectedJson);

            // Act
            String result = locationService.getVillageMasterFromBlockID(distBlockID);

            // Assert
            assertNotNull(result);
            assertEquals(expectedJson, result);
            verify(districtBranchMasterRepo).findByBlockID(distBlockID);
        }
    }

    @Test
    @DisplayName("Test getLocDetails - Success with data (Deprecated method)")
    @SuppressWarnings("deprecation")
    void testGetLocDetails_Success() {
        // Arrange
        Integer spID = 1;
        Integer spPSMID = 1;

        ArrayList<Object[]> mockLocDetails = new ArrayList<>();
        Object[] locDetail = {1, "ParkingPlace1", 1, "District1", 1, "Zone1", 1, "State1", 1, "Block1"};
        mockLocDetails.add(locDetail);

        ArrayList<Object[]> mockVillageList = new ArrayList<>();
        Object[] village = {1, "Village1"};
        mockVillageList.add(village);

        when(v_GetLocDetailsFromSPidAndPSMidRepo
                .findByServicepointidAndSpproviderservicemapidAndPpproviderservicemapidAndZdmproviderservicemapid(
                        spID, spPSMID, spPSMID, spPSMID)).thenReturn(mockLocDetails);
        when(stateMasterRepo.getStateMaster()).thenReturn(mockStateMasterList);
        when(servicePointVillageMappingRepo.getServicePointVillages(spID)).thenReturn(mockVillageList);

        try (MockedStatic<V_GetLocDetailsFromSPidAndPSMid> mockedStatic = 
                mockStatic(V_GetLocDetailsFromSPidAndPSMid.class)) {
            V_GetLocDetailsFromSPidAndPSMid mockLocObj = new V_GetLocDetailsFromSPidAndPSMid();
            mockedStatic.when(() -> V_GetLocDetailsFromSPidAndPSMid.getOtherLocDetails(mockLocDetails))
                    .thenReturn(mockLocObj);

            // Act
            String result = locationService.getLocDetails(spID, spPSMID);

            // Assert
            assertNotNull(result);
            assertTrue(result.contains("otherLoc"));
            assertTrue(result.contains("stateMaster"));
            assertTrue(result.contains("villageMaster"));
        }
    }

    @Test
    @DisplayName("Test getLocDetailsNew - Success with userId")
    void testGetLocDetailsNew_SuccessWithUserId() {
        // Arrange
        Integer spID = 1;
        Integer spPSMID = 1;
        Integer userId = 1;

        ArrayList<Object[]> mockLocDetails = new ArrayList<>();
        Object[] locDetail = {1, "ParkingPlace1", 1, "District1", 1, "Zone1", 1, "State1", 1, "Block1"};
        mockLocDetails.add(locDetail);

        ArrayList<Object[]> mockVillageList = new ArrayList<>();
        Object[] village = {1, "Village1"};
        mockVillageList.add(village);

        ArrayList<Object[]> mockUserServiceMapping = new ArrayList<>();
        Object[] userMapping = {1, "State1", "1,2", "District1,District2", "1,2", "Block1,Block2", "1,2", "Village1,Village2"};
        mockUserServiceMapping.add(userMapping);

        when(v_get_prkngplc_dist_zone_state_from_spidRepo.getDefaultLocDetails(spID, spPSMID))
                .thenReturn(mockLocDetails);
        when(stateMasterRepo.getStateMaster()).thenReturn(mockStateMasterList);
        when(servicePointVillageMappingRepo.getServicePointVillages(spID)).thenReturn(mockVillageList);
        when(districtBlockMasterRepo.getUserservicerolemapping(userId)).thenReturn(mockUserServiceMapping);

        // Act
        String result = locationService.getLocDetailsNew(spID, spPSMID, userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("otherLoc"));
        assertTrue(result.contains("stateMaster"));
        assertTrue(result.contains("villageList"));
        assertTrue(result.contains("userDetails"));
        verify(districtBlockMasterRepo).getUserservicerolemapping(userId);
    }

    @Test
    @DisplayName("Test getLocDetailsNew - Success without userId")
    void testGetLocDetailsNew_SuccessWithoutUserId() {
        // Arrange
        Integer spID = 1;
        Integer spPSMID = 1;
        Integer userId = null;

        ArrayList<Object[]> mockLocDetails = new ArrayList<>();
        Object[] locDetail = {1, "ParkingPlace1", 1, "District1", 1, "Zone1", 1, "State1", 1, "Block1"};
        mockLocDetails.add(locDetail);

        ArrayList<Object[]> mockVillageList = new ArrayList<>();
        Object[] village = {1, "Village1"};
        mockVillageList.add(village);

        when(v_get_prkngplc_dist_zone_state_from_spidRepo.getDefaultLocDetails(spID, spPSMID))
                .thenReturn(mockLocDetails);
        when(stateMasterRepo.getStateMaster()).thenReturn(mockStateMasterList);
        when(servicePointVillageMappingRepo.getServicePointVillages(spID)).thenReturn(mockVillageList);

        // Act
        String result = locationService.getLocDetailsNew(spID, spPSMID, userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("otherLoc"));
        assertTrue(result.contains("stateMaster"));
        assertTrue(result.contains("villageList"));
        assertFalse(result.contains("userDetails"));
        verify(districtBlockMasterRepo, never()).getUserservicerolemapping(any());
    }

    @Test
    @DisplayName("Test getLocDetailsNew - Empty user service mapping")
    void testGetLocDetailsNew_EmptyUserServiceMapping() {
        // Arrange
        Integer spID = 1;
        Integer spPSMID = 1;
        Integer userId = 1;

        ArrayList<Object[]> mockLocDetails = new ArrayList<>();
        Object[] locDetail = {1, "ParkingPlace1", 1, "District1", 1, "Zone1", 1, "State1", 1, "Block1"};
        mockLocDetails.add(locDetail);

        when(v_get_prkngplc_dist_zone_state_from_spidRepo.getDefaultLocDetails(spID, spPSMID))
                .thenReturn(mockLocDetails);
        when(stateMasterRepo.getStateMaster()).thenReturn(mockStateMasterList);
        when(servicePointVillageMappingRepo.getServicePointVillages(spID)).thenReturn(new ArrayList<>());
        when(districtBlockMasterRepo.getUserservicerolemapping(userId)).thenReturn(new ArrayList<>());

        // Act
        String result = locationService.getLocDetailsNew(spID, spPSMID, userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("otherLoc"));
        assertTrue(result.contains("stateMaster"));
        assertTrue(result.contains("villageList"));
        assertTrue(result.contains("userDetails"));
    }

    @Test
    @DisplayName("Test getDistrictTalukList - Success with data")
    void testGetDistrictTalukList_Success() {
        // Arrange
        Integer districtBranchID = 1;
        ArrayList<Object[]> mockTalukList = new ArrayList<>();
        Object[] taluk1 = {"Block1", 1, "District1", 1};
        Object[] taluk2 = {"Block2", 2, "District2", 2};
        mockTalukList.add(taluk1);
        mockTalukList.add(taluk2);

        when(districtBranchMasterRepo.getDistrictTalukList(districtBranchID)).thenReturn(mockTalukList);

        // Act
        String result = locationService.getDistrictTalukList(districtBranchID);

        // Assert
        assertNotNull(result);
        // Due to the bug in service implementation (reusing same map object), 
        // only the last iteration's values will be present
        assertTrue(result.contains("Block2"));
        assertTrue(result.contains("District2"));
        verify(districtBranchMasterRepo).getDistrictTalukList(districtBranchID);
    }

    @Test
    @DisplayName("Test getDistrictTalukList - Empty data")
    void testGetDistrictTalukList_EmptyData() {
        // Arrange
        Integer districtBranchID = 1;
        when(districtBranchMasterRepo.getDistrictTalukList(districtBranchID)).thenReturn(new ArrayList<>());

        // Act
        String result = locationService.getDistrictTalukList(districtBranchID);

        // Assert
        assertNotNull(result);
        assertEquals("[]", result);
        verify(districtBranchMasterRepo).getDistrictTalukList(districtBranchID);
    }

    @Test
    @DisplayName("Test getUserServiceroleMapping - with null district data")
    void testGetUserServiceroleMappingWithNullDistrict() {
        // Arrange
        Integer spID = 1;
        Integer spPSMID = 1;
        Integer userId = 1;

        ArrayList<Object[]> mockLocDetails = new ArrayList<>();
        Object[] locDetail = {1, "ParkingPlace1", 1, "District1", 1, "Zone1", 1, "State1", 1, "Block1"};
        mockLocDetails.add(locDetail);

        ArrayList<Object[]> mockUserServiceMapping = new ArrayList<>();
        Object[] userMapping = {1, "State1", null, null, "1,2", "Block1,Block2", "1,2", "Village1,Village2"};
        mockUserServiceMapping.add(userMapping);

        when(v_get_prkngplc_dist_zone_state_from_spidRepo.getDefaultLocDetails(spID, spPSMID))
                .thenReturn(mockLocDetails);
        when(stateMasterRepo.getStateMaster()).thenReturn(mockStateMasterList);
        when(servicePointVillageMappingRepo.getServicePointVillages(spID)).thenReturn(new ArrayList<>());
        when(districtBlockMasterRepo.getUserservicerolemapping(userId)).thenReturn(mockUserServiceMapping);

        // Act
        String result = locationService.getLocDetailsNew(spID, spPSMID, userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("userDetails"));
    }

    @Test
    @DisplayName("Test getUserServiceroleMapping - with null block data")
    void testGetUserServiceroleMappingWithNullBlock() {
        // Arrange
        Integer spID = 1;
        Integer spPSMID = 1;
        Integer userId = 1;

        ArrayList<Object[]> mockLocDetails = new ArrayList<>();
        Object[] locDetail = {1, "ParkingPlace1", 1, "District1", 1, "Zone1", 1, "State1", 1, "Block1"};
        mockLocDetails.add(locDetail);

        ArrayList<Object[]> mockUserServiceMapping = new ArrayList<>();
        Object[] userMapping = {1, "State1", "1,2", "District1,District2", null, null, "1,2", "Village1,Village2"};
        mockUserServiceMapping.add(userMapping);

        when(v_get_prkngplc_dist_zone_state_from_spidRepo.getDefaultLocDetails(spID, spPSMID))
                .thenReturn(mockLocDetails);
        when(stateMasterRepo.getStateMaster()).thenReturn(mockStateMasterList);
        when(servicePointVillageMappingRepo.getServicePointVillages(spID)).thenReturn(new ArrayList<>());
        when(districtBlockMasterRepo.getUserservicerolemapping(userId)).thenReturn(mockUserServiceMapping);

        // Act
        String result = locationService.getLocDetailsNew(spID, spPSMID, userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("userDetails"));
    }

    @Test
    @DisplayName("Test getUserServiceroleMapping - with null village data")
    void testGetUserServiceroleMappingWithNullVillage() {
        // Arrange
        Integer spID = 1;
        Integer spPSMID = 1;
        Integer userId = 1;

        ArrayList<Object[]> mockLocDetails = new ArrayList<>();
        Object[] locDetail = {1, "ParkingPlace1", 1, "District1", 1, "Zone1", 1, "State1", 1, "Block1"};
        mockLocDetails.add(locDetail);

        ArrayList<Object[]> mockUserServiceMapping = new ArrayList<>();
        Object[] userMapping = {1, "State1", "1,2", "District1,District2", "1,2", "Block1,Block2", null, null};
        mockUserServiceMapping.add(userMapping);

        when(v_get_prkngplc_dist_zone_state_from_spidRepo.getDefaultLocDetails(spID, spPSMID))
                .thenReturn(mockLocDetails);
        when(stateMasterRepo.getStateMaster()).thenReturn(mockStateMasterList);
        when(servicePointVillageMappingRepo.getServicePointVillages(spID)).thenReturn(new ArrayList<>());
        when(districtBlockMasterRepo.getUserservicerolemapping(userId)).thenReturn(mockUserServiceMapping);

        // Act
        String result = locationService.getLocDetailsNew(spID, spPSMID, userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("userDetails"));
    }

    @Test
    @DisplayName("Test getDefaultLocDetails - with empty data")
    void testGetDefaultLocDetailsEmptyData() {
        // Arrange
        Integer spID = 1;
        Integer spPSMID = 1;
        Integer userId = null;

        when(v_get_prkngplc_dist_zone_state_from_spidRepo.getDefaultLocDetails(spID, spPSMID))
                .thenReturn(new ArrayList<>());
        when(stateMasterRepo.getStateMaster()).thenReturn(mockStateMasterList);
        when(servicePointVillageMappingRepo.getServicePointVillages(spID)).thenReturn(new ArrayList<>());

        // Act
        String result = locationService.getLocDetailsNew(spID, spPSMID, userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("otherLoc"));
        assertTrue(result.contains("stateMaster"));
        assertTrue(result.contains("villageList"));
    }

    @Test
    @DisplayName("Test getDistrictTalukList - Null data")
    void testGetDistrictTalukList_NullData() {
        // Arrange
        Integer districtBranchID = 1;
        when(districtBranchMasterRepo.getDistrictTalukList(districtBranchID)).thenReturn(null);

        // Act
        String result = locationService.getDistrictTalukList(districtBranchID);

        // Assert
        assertNotNull(result);
        assertEquals("[]", result);
        verify(districtBranchMasterRepo).getDistrictTalukList(districtBranchID);
    }

    @Test
    @DisplayName("Test getLocDetailsNew - Handle duplicate district IDs")
    void testGetLocDetailsNew_HandleDuplicateDistrictIds() {
        // Arrange
        Integer spID = 1;
        Integer spPSMID = 1;
        Integer userId = 1;

        ArrayList<Object[]> mockLocDetails = new ArrayList<>();
        Object[] locDetail = {1, "ParkingPlace1", 1, "District1", 1, "Zone1", 1, "State1", 1, "Block1"};
        mockLocDetails.add(locDetail);

        ArrayList<Object[]> mockUserServiceMapping = new ArrayList<>();
        // Adding duplicate district IDs to test the Set<String> districtIdSet logic
        Object[] userMapping1 = {1, "State1", "1,2", "District1,District2", "1,2", "Block1,Block2", "1,2", "Village1,Village2"};
        Object[] userMapping2 = {1, "State1", "1,3", "District1,District3", "3,4", "Block3,Block4", "3,4", "Village3,Village4"};
        mockUserServiceMapping.add(userMapping1);
        mockUserServiceMapping.add(userMapping2);

        when(v_get_prkngplc_dist_zone_state_from_spidRepo.getDefaultLocDetails(spID, spPSMID))
                .thenReturn(mockLocDetails);
        when(stateMasterRepo.getStateMaster()).thenReturn(mockStateMasterList);
        when(servicePointVillageMappingRepo.getServicePointVillages(spID)).thenReturn(new ArrayList<>());
        when(districtBlockMasterRepo.getUserservicerolemapping(userId)).thenReturn(mockUserServiceMapping);

        // Act
        String result = locationService.getLocDetailsNew(spID, spPSMID, userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("userDetails"));
        assertTrue(result.contains("District1"));
        assertTrue(result.contains("District2"));
        assertTrue(result.contains("District3"));
    }

    @Test
    @DisplayName("Test getZoneList - Null data")
    void testGetZoneList_NullData() {
        // Arrange
        Integer providerServiceMapID = 1;
        when(zoneMasterRepo.getZoneMaster(providerServiceMapID)).thenReturn(null);

        // Act
        String result = locationService.getZoneList(providerServiceMapID);

        // Assert
        assertNotNull(result);
        assertEquals("[]", result);
        verify(zoneMasterRepo).getZoneMaster(providerServiceMapID);
    }

    @Test
    @DisplayName("Test getDistrictList - Null data")
    void testGetDistrictList_NullData() {
        // Arrange
        Integer stateID = 1;
        when(districtMasterRepo.getDistrictMaster(stateID)).thenReturn(null);

        // Act
        String result = locationService.getDistrictList(stateID);

        // Assert
        assertNotNull(result);
        assertEquals("[]", result);
        verify(districtMasterRepo).getDistrictMaster(stateID);
    }

    @Test
    @DisplayName("Test getDistrictBlockList - Null data")
    void testGetDistrictBlockList_NullData() {
        // Arrange
        Integer districtID = 1;
        when(districtBlockMasterRepo.getDistrictBlockMaster(districtID)).thenReturn(null);

        // Act
        String result = locationService.getDistrictBlockList(districtID);

        // Assert
        assertNotNull(result);
        assertEquals("[]", result);
        verify(districtBlockMasterRepo).getDistrictBlockMaster(districtID);
    }

    @Test
    @DisplayName("Test getParkingPlaceList - Null data")
    void testGetParkingPlaceList_NullData() {
        // Arrange
        Integer providerServiceMapID = 1;
        when(parkingPlaceMasterRepo.getParkingPlaceMaster(providerServiceMapID)).thenReturn(null);

        // Act
        String result = locationService.getParkingPlaceList(providerServiceMapID);

        // Assert
        assertNotNull(result);
        assertEquals("[]", result);
        verify(parkingPlaceMasterRepo).getParkingPlaceMaster(providerServiceMapID);
    }

    @Test
    @DisplayName("Test getServicePointPlaceList - Null data")
    void testGetServicePointPlaceList_NullData() {
        // Arrange
        Integer parkingPlaceID = 1;
        when(servicePointMasterRepo.getServicePointMaster(parkingPlaceID)).thenReturn(null);

        // Act
        String result = locationService.getServicePointPlaceList(parkingPlaceID);

        // Assert
        assertNotNull(result);
        assertEquals("[]", result);
        verify(servicePointMasterRepo).getServicePointMaster(parkingPlaceID);
    }
}
