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
package com.iemr.mmu.service.dataSyncActivity;

/**
 * Thrown when an upload is requested while another upload from this laptop is still running. Two uploads at the
 * same time read the same unprocessed rows and central inserts each of them twice.
 */
public class UploadInProgressException extends Exception {

	private static final long serialVersionUID = 1L;

	public static final String MESSAGE = "Data sync is already in progress. Please wait for it to finish.";

	public UploadInProgressException() {
		super(MESSAGE);
	}
}