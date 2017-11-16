/*
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.server.servlet;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static org.jboss.errai.common.client.framework.Constants.ERRAI_CSRF_TOKEN_HEADER;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jboss.errai.bus.server.util.SecureHashUtil;
import org.slf4j.Logger;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public final class CSRFTokenCheck implements RequestSecurityCheck {

  public static CSRFTokenCheck INSTANCE = new CSRFTokenCheck();
  public static final String CSRF_TOKEN_ATTRIBUTE_NAME = "errai.bus.csrf_token";

  private CSRFTokenCheck() {}

  @Override
  public boolean isInsecure(final HttpServletRequest request, final Logger log) {
    final HttpSession session = request.getSession(false);
    final String expectedToken = (session != null ? (String) session.getAttribute(CSRF_TOKEN_ATTRIBUTE_NAME) : null);
    final String observedToken = request.getHeader(ERRAI_CSRF_TOKEN_HEADER);

    if (expectedToken == null && session != null)
      log.warn("CSRF protection is enabled but no CSRF token was found for the HTTP session with id {}", session.getId());

    return session != null && (expectedToken == null || !expectedToken.equals(observedToken));
  }

  @Override
  public void prepareResponse(final HttpServletRequest request, final HttpServletResponse response, final Logger log) {
    final HttpSession session = request.getSession(false);
    if (session == null) {
      throw new IllegalStateException("Cannot create CSRF token challenge when session is null.");
    }

    prepareSession(session, log);

    final String token = (String) session.getAttribute(CSRF_TOKEN_ATTRIBUTE_NAME);
    response.setHeader(ERRAI_CSRF_TOKEN_HEADER, token);
    response.setStatus(SC_FORBIDDEN);
  }

  @Override
  public void prepareSession(final HttpSession session, final Logger log) {
    if (session.getAttribute(CSRF_TOKEN_ATTRIBUTE_NAME) == null) {
      final String token = SecureHashUtil.nextSecureHash();
      log.debug("Generated token [{}] for HTTP session with id [{}].", token, session.getId());
      session.setAttribute(CSRF_TOKEN_ATTRIBUTE_NAME, token);
    }
  }

  public static String getToken(final HttpSession session) {
    return (String) session.getAttribute(CSRF_TOKEN_ATTRIBUTE_NAME);
  }

}
