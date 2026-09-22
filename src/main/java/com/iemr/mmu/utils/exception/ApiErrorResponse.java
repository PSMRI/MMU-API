/*
* AMRIT â€“ Accessible Medical Records via Integrated Technology
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
package com.iemr.mmu.utils.exception;

import java.time.Instant;

public class ApiErrorResponse {
	private final Instant timestamp = Instant.now();
	private final int statusCode;
	private final String error;
	private final String errorMessage;
	private final String path;

	public ApiErrorResponse(int statusCode, String error, String errorMessage, String path) {
		this.statusCode = statusCode;
		this.error = error;
		this.errorMessage = errorMessage;
		this.path = path;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getError() {
		return error;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public String getPath() {
		return path;
	}
}
