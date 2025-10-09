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

import com.iemr.mmu.utils.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CookieUtilTest_Batch1 {

    @Mock
    private HttpServletRequest request;

    private CookieUtil cookieUtil;

    @BeforeEach
    void setUp() {
        cookieUtil = new CookieUtil();
    }

    @Test
    void getCookieValue_whenCookieExists_thenReturnCookieValue() {
        String cookieName = "testCookie";
        String cookieValue = "testValue";
        Cookie cookie = new Cookie(cookieName, cookieValue);
        Cookie[] cookies = {new Cookie("otherCookie", "otherValue"), cookie};
        when(request.getCookies()).thenReturn(cookies);

        Optional<String> result = cookieUtil.getCookieValue(request, cookieName);

        assertTrue(result.isPresent());
        assertEquals(cookieValue, result.get());
    }

    @Test
    void getCookieValue_whenCookieDoesNotExist_thenReturnEmptyOptional() {
        String cookieName = "nonExistentCookie";
        Cookie[] cookies = {new Cookie("otherCookie", "otherValue")};
        when(request.getCookies()).thenReturn(cookies);

        Optional<String> result = cookieUtil.getCookieValue(request, cookieName);

        assertFalse(result.isPresent());
    }

    @Test
    void getCookieValue_whenRequestHasNoCookies_thenReturnEmptyOptional() {
        when(request.getCookies()).thenReturn(null);

        Optional<String> result = cookieUtil.getCookieValue(request, "anyCookie");

        assertFalse(result.isPresent());
    }

    @Test
    void getCookieValue_whenRequestHasEmptyCookieArray_thenReturnEmptyOptional() {
        when(request.getCookies()).thenReturn(new Cookie[0]);

        Optional<String> result = cookieUtil.getCookieValue(request, "anyCookie");

        assertFalse(result.isPresent());
    }

    @Test
    void getJwtTokenFromCookie_whenJwtCookieExists_thenReturnToken() {
        String jwtToken = "some.jwt.token";
        Cookie jwtCookie = new Cookie("Jwttoken", jwtToken);
        Cookie[] cookies = {new Cookie("otherCookie", "otherValue"), jwtCookie};
        when(request.getCookies()).thenReturn(cookies);

        String result = CookieUtil.getJwtTokenFromCookie(request);

        assertNotNull(result);
        assertEquals(jwtToken, result);
    }

    @Test
    void getJwtTokenFromCookie_whenJwtCookieDoesNotExist_thenReturnNull() {
        Cookie[] cookies = {new Cookie("otherCookie", "otherValue")};
        when(request.getCookies()).thenReturn(cookies);

        String result = CookieUtil.getJwtTokenFromCookie(request);

        assertNull(result);
    }

    @Test
    void getJwtTokenFromCookie_whenRequestHasNoCookies_thenReturnNull() {
        when(request.getCookies()).thenReturn(null);

        String result = CookieUtil.getJwtTokenFromCookie(request);

        assertNull(result);
    }

    @Test
    void getJwtTokenFromCookie_whenRequestHasEmptyCookieArray_thenReturnNull() {
        when(request.getCookies()).thenReturn(new Cookie[0]);

        String result = CookieUtil.getJwtTokenFromCookie(request);

        assertNull(result);
    }
}