/**
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.security.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.errai.marshalling.server.MappingContextSingleton;
import org.picketlink.authentication.web.HTTPAuthenticationScheme;
import org.picketlink.credential.DefaultLoginCredentials;

@Dependent
public class FormAuthenticationScheme implements HTTPAuthenticationScheme {

  public static final String HOST_PAGE_INIT_PARAM = "host-page";
  public static final String LOGIN_PAGE_INIT_PARAM = "form-login-page";

  public static final String LOGIN_ERROR_QUERY_PARAM = "login_failed";

  public static final String HTTP_FORM_SECURITY_CHECK_URI = "/uf_security_check";
  public static final String HTTP_FORM_USERNAME_PARAM = "uf_username";
  public static final String HTTP_FORM_PASSWORD_PARAM = "uf_password";

  /**
   * URI of the GWT host page, relative to the servlet container root (so it starts with '/' and includes the context
   * path).
   */
  private String hostPageUri;

  /**
   * URI of the login page, relative to the servlet container root (so it starts with '/' and includes the context
   * path).
   */
  private String loginPageUri;

  @Override
  public void initialize(FilterConfig filterConfig) {
    String contextRelativeHostPageUri = filterConfig.getInitParameter(HOST_PAGE_INIT_PARAM);
    if (contextRelativeHostPageUri == null) {
      throw new IllegalStateException(
              "FormAuthenticationScheme requires that you set the filter init parameter \""
                      + HOST_PAGE_INIT_PARAM + "\" to the context-relative URI of the host page.");
    }
    hostPageUri = filterConfig.getServletContext().getContextPath() + contextRelativeHostPageUri;

    String contextRelativeLoginPageUri = filterConfig.getInitParameter(LOGIN_PAGE_INIT_PARAM);
    if (contextRelativeLoginPageUri == null) {
      throw new IllegalStateException(
              "FormAuthenticationScheme requires that you set the filter init parameter \""
                      + LOGIN_PAGE_INIT_PARAM + "\" to the context-relative URI of the login page.");
    }
    loginPageUri = filterConfig.getServletContext().getContextPath() + contextRelativeLoginPageUri;

    // this ensures Errai Marshalling has been set up (for encoding the cookie)
    MappingContextSingleton.get();
  }

  @Override
  public void extractCredential(HttpServletRequest request, DefaultLoginCredentials creds) {
    if (isLoginAttempt(request)) {
      creds.setUserId(request.getParameter(HTTP_FORM_USERNAME_PARAM));
      creds.setPassword(request.getParameter(HTTP_FORM_PASSWORD_PARAM));
    }
  }

  @Override
  public void challengeClient(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (request.getRequestURI().equals(hostPageUri)) {
      StringBuilder loginUri = new StringBuilder();
      loginUri.append(loginPageUri);

      String extraParams = extractParameters(request);
      if (extraParams.length() > 0) {
        loginUri.append("?").append(extraParams);
      }

      response.sendRedirect(loginUri.toString());
    }
    else if (isLoginAttempt(request)) {
      StringBuilder loginUri = new StringBuilder();
      loginUri.append(loginPageUri);
      loginUri.append("?");
      loginUri.append(LOGIN_ERROR_QUERY_PARAM).append("=true");

      String extraParams = extractParameters(request);
      if (extraParams.length() > 0) {
        loginUri.append("&").append(extraParams);
      }

      response.sendRedirect(loginUri.toString());
    }
    else {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }
  }

  private boolean isLoginAttempt(HttpServletRequest request) {
    return request.getMethod().equals("POST") && request.getRequestURI().contains(HTTP_FORM_SECURITY_CHECK_URI);
  }

  @Override
  public boolean postAuthentication(HttpServletRequest request, HttpServletResponse response) throws IOException {
    StringBuilder redirectTarget = new StringBuilder(hostPageUri);
    String extraParams = extractParameters(request);
    if (extraParams.length() > 0) {
      redirectTarget.append("?").append(extraParams);
    }

    response.sendRedirect(redirectTarget.toString());
    return false;
  }

  /**
   * Extracts all parameters except the username and password into a URL-encoded query string. The string does not begin
   * or end with a "&amp;".
   */
  private static String extractParameters(HttpServletRequest fromRequest) {
    try {
      StringBuilder sb = new StringBuilder();
      for (Map.Entry<String, String[]> param : fromRequest.getParameterMap().entrySet()) {
        String paramName = URLEncoder.encode(param.getKey(), "UTF-8");
        if (paramName.equals(HTTP_FORM_USERNAME_PARAM) || paramName.equals(HTTP_FORM_PASSWORD_PARAM)) {
          continue;
        }
        for (String value : param.getValue()) {
          if (sb.length() != 0) {
            sb.append("&");
          }
          sb.append(paramName).append("=").append(URLEncoder.encode(value, "UTF-8"));
        }
      }
      return sb.toString();
    } catch (UnsupportedEncodingException e) {
      throw new AssertionError("UTF-8 not supported on this JVM?");
    }
  }

  @Override
  public boolean isProtected(HttpServletRequest request) {
    return true;
  }
}
