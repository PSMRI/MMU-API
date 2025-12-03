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
package com.iemr.mmu.repo.quickConsultation;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iemr.mmu.data.quickConsultation.LabTestOrderDetail;

import jakarta.transaction.Transactional;

@Repository
public interface LabTestOrderDetailRepo extends CrudRepository<LabTestOrderDetail, Long>{

	@Query(" SELECT beneficiaryRegID, benVisitID, providerServiceMapID,	procedureID, procedureName, "
			+ "testingRequirements, visitCode  from LabTestOrderDetail ba WHERE ba.beneficiaryRegID = :benRegID "
			+ "AND ba.visitCode= :visitCode")
	public ArrayList<Object[]> getLabTestOrderDetails(@Param("benRegID") Long benRegID,
			@Param("visitCode") Long visitCode);
	
	@Modifying
	@Transactional
	@Query(" Delete from LabTestOrderDetail WHERE beneficiaryRegID = :benRegID AND benVisitID = :benVisitID ")
	public int deleteExistingLabTestOrderDetail(@Param("benRegID") Long benRegID, @Param("benVisitID") Long benVisitID);
	
	@Transactional
	@Modifying
	@Query(" UPDATE LabTestOrderDetail set vanSerialNo = :labTestOrderID WHERE labTestOrderID = :labTestOrderID")
	int updateVanSerialNo(@Param("labTestOrderID") Long labTestOrderID);
}
