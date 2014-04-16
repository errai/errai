/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.errai.security.server.tmp;

import java.io.IOException;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.picketlink.credential.DefaultLoginCredentials;

/**
 * NOTE: This type is temporarily in Errai until the next PicketLink 6.0 release with our contributed modifications
 * comes out.
 * <p>
 * Basis for the HTTP Authentication Schemes such as BASIC, FORM, DIGEST and CLIENT-CERT. Applications can provide their
 * own authentication schemes by implementing this interface. Implementations must be valid CDI beans, and do support
 * injection.
 * 
 * @author Pedro Silva
 * @author Jonathan Fuerth
 * @author Max Barkley
 */
public interface HTTPAuthenticationScheme {

  /**
   * Called one time by the {@link AuthenticationFilter} after the CDI initialization has completed, but before any
   * other methods from this interface are invoked.
   * 
   * @param config
   *          the configuration of {@link AuthenticationFilter} from {@code web.xml}. Never null.
   */
  void initialize(FilterConfig config);

  /**
   * Extracts the credentials from the given {@link HttpServletRequest} and populates the
   * {@link DefaultLoginCredentials} with them. If the request is not an authentication attempt (as defined by the
   * implementation), then {@code creds} is not affected.
   * 
   * @param request
   *          The current request, to examine for authentication information.
   * @param creds
   *          The credentials instance that will be populated with the credentials found in the request, if any.
   */
  void extractCredential(HttpServletRequest request, DefaultLoginCredentials creds);

  /**
   * Challenges the client if no credentials were supplied or the credentials were not extracted in order to continue
   * with the authentication.
   * 
   * @param request
   *          The current request, which may be used to obtain a {@link javax.servlet.RequestDispatcher} if needed. If
   *          this method is called, the rest of the filter chain will <i>not</i> be processed, so implementations are
   *          free to read the request body if they so choose.
   * @param response
   *          The current response, which can be used to send HTTP error results, redirects, or for sending additional
   *          challenge headers.
   * @throws IOException
   *           if reading the request or writing the response fails.
   */
  void challengeClient(HttpServletRequest request, HttpServletResponse response) throws IOException;

  /**
   * Performs any post-authentication logic regarding of the authentication result.
   * 
   * @param request
   *          The current request, which may be used to obtain a {@link javax.servlet.RequestDispatcher} if needed.
   * @param response
   *          The current response, which can be used to send an HTTP response, or a redirect.
   * @return true if the processing of the filter chain should continue, false if the processing should stop (typically
   *         because this filter has already sent a response).
   * @throws IOException
   *           if reading the request or writing the response fails.
   */
  boolean postAuthentication(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
