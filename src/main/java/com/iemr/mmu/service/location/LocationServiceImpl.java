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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.iemr.mmu.data.location.Country;
import com.iemr.mmu.data.location.CountryCityMaster;
import com.iemr.mmu.data.location.DistrictBlock;
import com.iemr.mmu.data.location.DistrictBranchMapping;
import com.iemr.mmu.data.location.Districts;
import com.iemr.mmu.data.location.States;
import com.iemr.mmu.data.location.V_GetLocDetailsFromSPidAndPSMid;
import com.iemr.mmu.data.location.ZoneMaster;
import com.iemr.mmu.data.login.MasterServicePoint;
import com.iemr.mmu.data.login.ParkingPlace;
import com.iemr.mmu.data.login.ServicePointVillageMapping;
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

@Service
public class LocationServiceImpl implements LocationService {
	@Autowired
	private CountryMasterRepo countryMasterRepo;
	@Autowired
	private CountryCityMasterRepo countryCityMasterRepo;

	private StateMasterRepo stateMasterRepo;
	private ZoneMasterRepo zoneMasterRepo;
	private DistrictMasterRepo districtMasterRepo;
	private DistrictBlockMasterRepo districtBlockMasterRepo;
	private ParkingPlaceMasterRepo parkingPlaceMasterRepo;
	private ServicePointMasterRepo servicePointMasterRepo;
	private V_GetLocDetailsFromSPidAndPSMidRepo v_GetLocDetailsFromSPidAndPSMidRepo;
	private ServicePointVillageMappingRepo servicePointVillageMappingRepo;
	private DistrictBranchMasterRepo districtBranchMasterRepo;
	private V_get_prkngplc_dist_zone_state_from_spidRepo v_get_prkngplc_dist_zone_state_from_spidRepo;

	@Autowired
	public void setV_get_prkngplc_dist_zone_state_from_spidRepo(
			V_get_prkngplc_dist_zone_state_from_spidRepo v_get_prkngplc_dist_zone_state_from_spidRepo) {
		this.v_get_prkngplc_dist_zone_state_from_spidRepo = v_get_prkngplc_dist_zone_state_from_spidRepo;
	}

	@Autowired
	public void setDistrictBranchMasterRepo(DistrictBranchMasterRepo districtBranchMasterRepo) {
		this.districtBranchMasterRepo = districtBranchMasterRepo;
	}

	@Autowired
	public void setServicePointVillageMappingRepo(ServicePointVillageMappingRepo servicePointVillageMappingRepo) {
		this.servicePointVillageMappingRepo = servicePointVillageMappingRepo;
	}

	@Autowired
	public void setV_GetLocDetailsFromSPidAndPSMidRepo(
			V_GetLocDetailsFromSPidAndPSMidRepo v_GetLocDetailsFromSPidAndPSMidRepo) {
		this.v_GetLocDetailsFromSPidAndPSMidRepo = v_GetLocDetailsFromSPidAndPSMidRepo;
	}

	@Autowired
	public void setServicePointMasterRepo(ServicePointMasterRepo servicePointMasterRepo) {
		this.servicePointMasterRepo = servicePointMasterRepo;
	}

	@Autowired
	public void setParkingPlaceMasterRepo(ParkingPlaceMasterRepo parkingPlaceMasterRepo) {
		this.parkingPlaceMasterRepo = parkingPlaceMasterRepo;
	}

	@Autowired
	public void setDistrictBlockMasterRepo(DistrictBlockMasterRepo districtBlockMasterRepo) {
		this.districtBlockMasterRepo = districtBlockMasterRepo;
	}

	@Autowired
	public void setDistrictMasterRepo(DistrictMasterRepo districtMasterRepo) {
		this.districtMasterRepo = districtMasterRepo;
	}

	@Autowired
	public void setZoneMasterRepo(ZoneMasterRepo zoneMasterRepo) {
		this.zoneMasterRepo = zoneMasterRepo;
	}

	@Autowired
	public void setStateMasterRepo(StateMasterRepo stateMasterRepo) {
		this.stateMasterRepo = stateMasterRepo;
	}

	@Override
	public String getStateList() {
		ArrayList<States> stateList = new ArrayList<>();
		ArrayList<Object[]> stateMasterList = stateMasterRepo.getStateMaster();
		if (stateMasterList != null && stateMasterList.size() > 0) {
			for (Object[] objArr : stateMasterList) {
				States states = new States((Integer) objArr[0], (String) objArr[1]);
				stateList.add(states);
			}
		}
		return new Gson().toJson(stateList);
	}

	@Override
	public String getCountryList() {

		ArrayList<Country> stateMasterList = countryMasterRepo.findAllCountries();

		return new Gson().toJson(stateMasterList);
	}

	@Override
	public String getCountryCityList(Integer countryID) {

		ArrayList<CountryCityMaster> countryCityList = countryCityMasterRepo.findByCountryIDAndDeleted(countryID,
				false);

		return new Gson().toJson(countryCityList);
	}

	@Override
	public String getZoneList(Integer providerServiceMapID) {
		ArrayList<Object> zoneList = new ArrayList<>();
		ArrayList<Object[]> zoneMasterList = zoneMasterRepo.getZoneMaster(providerServiceMapID);
		if (zoneMasterList != null && zoneMasterList.size() > 0) {
			for (Object[] objArr : zoneMasterList) {
				ZoneMaster zoneMaster = new ZoneMaster((Integer) objArr[0], (String) objArr[1]);
				zoneList.add(zoneMaster);
			}
		}
		return new Gson().toJson(zoneList);
	}

	@Override
	public String getDistrictList(Integer stateID) {
		ArrayList<Object> districtList = new ArrayList<>();
		ArrayList<Object[]> districtMasterList = districtMasterRepo.getDistrictMaster(stateID);
		if (districtMasterList != null && districtMasterList.size() > 0) {
			for (Object[] objArr : districtMasterList) {
				Districts districtMaster = new Districts((Integer) objArr[0], (String) objArr[1]);
				districtList.add(districtMaster);
			}
		}
		return new Gson().toJson(districtList);
	}

	@Override
	public String getDistrictBlockList(Integer districtID) {
		ArrayList<Object> districtBlockList = new ArrayList<>();
		ArrayList<Object[]> districtBlockMasterList = districtBlockMasterRepo.getDistrictBlockMaster(districtID);
		if (districtBlockMasterList != null && districtBlockMasterList.size() > 0) {
			for (Object[] objArr : districtBlockMasterList) {
				DistrictBlock districtBLockMaster = new DistrictBlock((Integer) objArr[0], (String) objArr[1]);
				districtBlockList.add(districtBLockMaster);
			}
		}
		return new Gson().toJson(districtBlockList);
	}

	@Override
	public String getParkingPlaceList(Integer providerServiceMapID) {
		ArrayList<Object> parkingPlaceList = new ArrayList<>();
		ArrayList<Object[]> parkingPlaceMasterList = parkingPlaceMasterRepo.getParkingPlaceMaster(providerServiceMapID);
		if (parkingPlaceMasterList != null && parkingPlaceMasterList.size() > 0) {
			for (Object[] objArr : parkingPlaceMasterList) {
				ParkingPlace parkingPlace = new ParkingPlace((Integer) objArr[0], (String) objArr[1]);
				parkingPlaceList.add(parkingPlace);
			}
		}
		return new Gson().toJson(parkingPlaceList);
	}

	public String getServicePointPlaceList(Integer parkingPlaceID) {
		ArrayList<Object> servicePointList = new ArrayList<>();
		ArrayList<Object[]> servicePointMasterList = servicePointMasterRepo.getServicePointMaster(parkingPlaceID);
		if (servicePointMasterList != null && servicePointMasterList.size() > 0) {
			for (Object[] objArr : servicePointMasterList) {
				MasterServicePoint masterServicePoint = new MasterServicePoint((Integer) objArr[0], (String) objArr[1]);
				servicePointList.add(masterServicePoint);
			}
		}
		return new Gson().toJson(servicePointList);
	}

	public String getVillageMasterFromBlockID(Integer distBlockID) {
		ArrayList<Object[]> resList = districtBranchMasterRepo.findByBlockID(distBlockID);
		return DistrictBranchMapping.getVillageList(resList);
	}

	// old, 11-10-2018
	@Deprecated

	public String getLocDetails(Integer spID, Integer spPSMID) {
		Map<String, Object> resMap = new HashMap<String, Object>();
		// other location details
		ArrayList<Object[]> objList = v_GetLocDetailsFromSPidAndPSMidRepo
				.findByServicepointidAndSpproviderservicemapidAndPpproviderservicemapidAndZdmproviderservicemapid(spID,
						spPSMID, spPSMID, spPSMID);

		V_GetLocDetailsFromSPidAndPSMid locOBJ = V_GetLocDetailsFromSPidAndPSMid.getOtherLocDetails(objList);

		// state master
		ArrayList<States> stateList = new ArrayList<>();
		ArrayList<Object[]> stateMasterList = stateMasterRepo.getStateMaster();
		if (stateMasterList != null && stateMasterList.size() > 0) {
			for (Object[] objArr : stateMasterList) {
				States states = new States((Integer) objArr[0], (String) objArr[1]);
				stateList.add(states);
			}
		}
		// village masters from service point
		List<Object[]> servicePointVillageList = servicePointVillageMappingRepo.getServicePointVillages(spID);

		ArrayList<ServicePointVillageMapping> villageList = new ArrayList<ServicePointVillageMapping>();
		if (servicePointVillageList.size() > 0) {
			ServicePointVillageMapping VillageMap;
			for (Object[] obj : servicePointVillageList) {
				VillageMap = new ServicePointVillageMapping((Integer) obj[0], (String) obj[1]);
				villageList.add(VillageMap);
			}
		}

		resMap.put("otherLoc", locOBJ);
		resMap.put("stateMaster", stateList);
		resMap.put("villageMaster", villageList);

		return new Gson().toJson(resMap);
	}

	// new, 11-10-2018
	public String getLocDetailsNew(Integer spID, Integer spPSMID, Integer userId) {
		Map<String, Object> resMap = new HashMap<String, Object>();
		// other location details
		ArrayList<Object[]> objList = v_get_prkngplc_dist_zone_state_from_spidRepo.getDefaultLocDetails(spID, spPSMID);

		// state master
		ArrayList<States> stateList = new ArrayList<>();
		ArrayList<Object[]> stateMasterList = stateMasterRepo.getStateMaster();
		if (stateMasterList != null && stateMasterList.size() > 0) {
			for (Object[] objArr : stateMasterList) {
				States states = new States((Integer) objArr[0], (String) objArr[1]);
				stateList.add(states);
			}
		}
		// Village list for servicepoint
		ArrayList<ServicePointVillageMapping> villageList = new ArrayList<>();
		List<Object[]> villageMasterList = servicePointVillageMappingRepo.getServicePointVillages(spID);
		if (villageMasterList != null && villageMasterList.size() > 0) {
			for (Object[] objArr : villageMasterList) {
				ServicePointVillageMapping villages = new ServicePointVillageMapping((Integer) objArr[0],
						(String) objArr[1]);
				villageList.add(villages);
			}
		}
	//get Block list for servicepoint
		if(null != userId) {
		List<Object[]> userServiceroleMapping = districtBlockMasterRepo.getUserservicerolemapping(userId);
			resMap.put("userDetails", getUserServiceroleMapping(userServiceroleMapping));
		}
		
		resMap.put("otherLoc", getDefaultLocDetails(objList));
		resMap.put("stateMaster", stateList);
		resMap.put("villageList", villageList);
		// resMap.put("villageMaster", villageList);

		return new Gson().toJson(resMap);
	}

	private Map<String, Object> getUserServiceroleMapping(List<Object[]> userServiceroleMapping) {
		Map<String, Object> returnObj = new HashMap<>();
		ArrayList<Map<String, Object>> districtList = new ArrayList<>();
		ArrayList<Map<String, Object>> blockList = new ArrayList<>();
		ArrayList<Map<String, Object>> villageList = new ArrayList<>();
		Map<String, Object> districtMap = new HashMap<>();
		Map<String, Object> blockMap = new HashMap<>();
		Map<String, Object> villageMap = new HashMap<>();
		
		Set<String> districtIdSet = new HashSet<>();
		
		if (userServiceroleMapping != null && userServiceroleMapping.size() > 0) {
			returnObj.put("stateID", userServiceroleMapping.get(0)[0]);
			returnObj.put("stateName", userServiceroleMapping.get(0)[1]);
			
			for (Object[] objArray : userServiceroleMapping) {
				
				if (null != objArray[2]) {
						String[] districtIds = objArray[2].toString().split(",");
						String[] districtNames = objArray[3].toString().split(",");
						int districtLength = districtIds.length;
						for (int i = 0; i < districtLength; i++) {
							if (!districtIdSet.contains(districtIds[i])) {
							districtMap = new HashMap<>();
							districtMap.put("districtID", districtIds[i]);
							districtMap.put("districtName", districtNames[i]);
							districtList.add(districtMap);
							districtIdSet.add(districtIds[i]);
						}
					}
				}
				
				if (null != objArray[4]) {
					String[] blockIds = objArray[4].toString().split(",");
					String[] blockName = objArray[5].toString().split(",");
					int blockLength = blockIds.length;
					for (int i = 0; i < blockLength; i++) {
						blockMap = new HashMap<>();
						blockMap.put("blockId", blockIds[i]);
						blockMap.put("blockName", blockName[i]);
						blockList.add(blockMap);
					}
				}
				if (null != objArray[6]) {
					String[] villageId = objArray[6].toString().split(",");
					String[] villageName = objArray[7].toString().split(",");
					int villageLength = villageId.length;
					for (int i = 0; i < villageLength; i++) {
						villageMap = new HashMap<>();
						villageMap.put("villageId", villageId[i]);
						villageMap.put("villageName", villageName[i]);
						villageList.add(villageMap);
					}
				}
			}
			returnObj.put("districtList", districtList);
			returnObj.put("blockList", blockList);
			returnObj.put("villageList", villageList);

		}
		return returnObj;
	}

	private Map<String, Object> getDefaultLocDetails(ArrayList<Object[]> objList) {
		Map<String, Object> returnObj = new HashMap<>();
		Map<String, Object> distMap = new HashMap<>();
		ArrayList<Map<String, Object>> distLit = new ArrayList<>();
		if (objList != null && objList.size() > 0) {
			returnObj.put("stateID", objList.get(0)[6]);
			returnObj.put("stateName", objList.get(0)[7]);
			returnObj.put("zoneID", objList.get(0)[4]);
			returnObj.put("zoneName", objList.get(0)[5]);
			returnObj.put("parkingPlaceID", objList.get(0)[0]);
			returnObj.put("parkingPlaceName", objList.get(0)[1]);
			for (Object[] objArr : objList) {
				distMap = new HashMap<>();
				distMap.put("districtID", objArr[2]);
				distMap.put("districtName", objArr[3]);
				distMap.put("blockID", objArr[8]);
				distMap.put("blockName", objArr[9]);

				distLit.add(distMap);
			}

			returnObj.put("districtList", distLit);
		}
		return returnObj;

	}

	@Override
	public String getDistrictTalukList(Integer districtBranchID) {

		ArrayList<Object[]> districtTalukeMasterList = districtBranchMasterRepo.getDistrictTalukList(districtBranchID);
		
		ArrayList<Object> distTalukList = new ArrayList<>();
		Map<String, Object> distTalukMap = new HashMap<String, Object>();
		if (districtTalukeMasterList != null && districtTalukeMasterList.size() > 0) {
			for (Object[] objArr : districtTalukeMasterList) {
				distTalukMap.put("blockName", objArr[0]);
				distTalukMap.put("blockID", objArr[1]);
				distTalukMap.put("districtName", objArr[2]);
				distTalukMap.put("districtID", objArr[3]);
				distTalukList.add(distTalukMap);
			}
		}
		return new Gson().toJson(distTalukList);
	}

}
