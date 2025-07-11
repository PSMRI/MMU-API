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