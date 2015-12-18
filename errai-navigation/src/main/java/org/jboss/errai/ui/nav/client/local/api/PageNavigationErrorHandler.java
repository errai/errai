/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.ui.nav.client.local.api;

import org.jboss.errai.ui.nav.client.local.UniquePageRole;

/**
 * Defines an error handler used for page navigation errors.
 * 
 * @author Divya Dadlani <ddadlani@redhat.com>
 */
public interface PageNavigationErrorHandler {

  /**
   * @param exception
   *          The exception that occurs, triggering the error handler code.
   * @param pageName
   *          The name of the page which we tried to navigate to.
   */
  public void handleInvalidPageNameError(Exception exception, String pageName);

  /**
   * @param exception
   *          The exception that occurs, triggering the error handler code.
   * @param pageRole
   *          The role of the page which we tried to navigate to.
   */
  public void handleError(Exception exception, Class<? extends UniquePageRole> pageRole);

  /**
   * 
   * @param exception
   *          The exception that occurs, triggering the error handler code.
   * @param urlPath
   *          The URL path which we tried to navigate to.
   */
  public void handleInvalidURLError(Exception exception, String urlPath);
}
