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
package com.iemr.mmu.data.masterdata.nurse;

import java.sql.Timestamp;
import java.util.ArrayList;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.google.gson.annotations.Expose;

@Entity
@Table(name = "m_visitcategory")
public class VisitCategory {
	@Id
	@GeneratedValue
	@Expose
	@Column(name = "VisitCategoryID")
	private Short visitCategoryID;
	
	@Expose
	@Column(name = "VisitCategory")
	private String visitCategory;
	
	@Expose
	@Column(name = "VisitCategoryDesc")
	private String visitCategoryDesc;
	
	@Expose
	@Column(name = "Deleted")
	private Boolean deleted;

	public VisitCategory(Short visitCategoryID, String visitCategory) {
		super();
		this.visitCategoryID = visitCategoryID;
		this.visitCategory = visitCategory;
	}

	public static ArrayList<VisitCategory> getVisitCategoryMasterData(ArrayList<Object[]> resList) {
		ArrayList<VisitCategory> resArray = new ArrayList<>();
		for (Object[] obj : resList) {
			VisitCategory cOBJ = new VisitCategory((Short) obj[0], (String) obj[1]);
			resArray.add(cOBJ);
		}
		return resArray;
	}

	
	
}
