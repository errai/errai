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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public interface RequestSecurityCheck {

  boolean isInsecure(HttpServletRequest request, Logger log);
  void prepareResponse(HttpServletRequest request, HttpServletResponse response, Logger log);
  void prepareSession(HttpSession session, Logger log);

  static RequestSecurityCheck noCheck() {
    return new RequestSecurityCheck() {

      @Override
      public void prepareResponse(final HttpServletRequest request, final HttpServletResponse response, final Logger log) {}

      @Override
      public boolean isInsecure(final HttpServletRequest request, final Logger log) {
        return false;
      }

      @Override
      public void prepareSession(final HttpSession session, final Logger log) {}
    };
  }

}
