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
package com.iemr.mmu.utils.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

class SecurityExceptionHandlersTest {

	@Test
	@DisplayName("an IEMR exception carries its message and error code")
	void iemrException_carriesItsMessageAndErrorCode() {
		IEMRException withCause = new IEMRException("wrapped", new RuntimeException("cause"));
		assertEquals("wrapped", withCause.getMessage());
		assertEquals("wrapped", withCause.toString());
		assertNull(withCause.getErrorCode());

		IEMRException withCode = new IEMRException("coded", 5002);
		assertEquals(5002, withCode.getErrorCode());

		withCode.setErrorCode(400);
		assertEquals(400, withCode.getErrorCode());
	}

	@Test
	@DisplayName("a denied request is answered with a 403 and a JSON body")
	void customAccessDeniedHandler_answersWithForbidden() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();

		new CustomAccessDeniedHandler().handle(new MockHttpServletRequest(), response,
				new AccessDeniedException("denied"));

		assertEquals(403, response.getStatus());
		assertEquals("application/json", response.getContentType());
		assertTrue(response.getContentAsString().contains("Access denied"));
	}

	@Test
	@DisplayName("an unauthenticated request is answered with a 401 and a JSON body")
	void customAuthenticationEntryPoint_answersWithUnauthorized() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();

		new CustomAuthenticationEntryPoint().commence(new MockHttpServletRequest(), response,
				new BadCredentialsException("no token"));

		assertEquals(401, response.getStatus());
		assertTrue(response.getContentAsString().contains("Unauthorized"));
		assertTrue(response.getContentAsString().contains("no token"));
	}
}
