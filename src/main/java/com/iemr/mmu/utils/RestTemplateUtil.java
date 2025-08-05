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
package com.iemr.mmu.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

public class RestTemplateUtil {
	private final static Logger logger = LoggerFactory.getLogger(RestTemplateUtil.class);
	
	public static HttpEntity<Object> createRequestEntity(Object body, String authorization, String jwtToken) {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
   
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE + ";charset=utf-8");

    if (authorization != null && !authorization.isEmpty()) {
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + authorization);
    }

	 if (jwtToken == null || jwtToken.isEmpty()) {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                try {
                    jwtToken = CookieUtil.getJwtTokenFromCookie(request);
                } catch (Exception e) {
                    logger.error("Error while getting JWT token from cookie: {}", e.getMessage());
                }
            }
        }


	  if (jwtToken != null && !jwtToken.isEmpty()) {
        headers.add(HttpHeaders.COOKIE, "Jwttoken=" + jwtToken);
    }
    

    return new HttpEntity<>(body, headers);
}
}