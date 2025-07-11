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
package com.iemr.mmu.repo.location;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iemr.mmu.data.location.DistrictBlock;

@Repository
public interface DistrictBlockMasterRepo extends CrudRepository<DistrictBlock, Integer> {
	@Query(" SELECT blockID, blockName FROM DistrictBlock WHERE districtID = :districtID AND deleted != true ")
	public ArrayList<Object[]> getDistrictBlockMaster(@Param("districtID") Integer districtID);
	
	@Query(value = " SELECT distinct StateID, StateName,WorkingDistrictID,WorkingDistrictName,blockid,blockname,villageid,villagename FROM db_iemr.v_userservicerolemapping WHERE UserID = :userId and UserServciceRoleDeleted is false",nativeQuery = true)
	public List<Object[]> getUserservicerolemapping(@Param("userId") Integer userId);

}
