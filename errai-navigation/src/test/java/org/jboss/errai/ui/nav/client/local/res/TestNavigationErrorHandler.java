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

package org.jboss.errai.ui.nav.client.local.res;

import org.jboss.errai.ui.nav.client.local.UniquePageRole;
import org.jboss.errai.ui.nav.client.local.api.PageNavigationErrorHandler;

public class TestNavigationErrorHandler implements PageNavigationErrorHandler {
  public int count;

  @Override
  public void handleInvalidPageNameError(Exception exception, String pageName) {
    count++;
  }

  @Override
  public void handleError(Exception exception, Class<? extends UniquePageRole> pageRole) {
    handleInvalidPageNameError(exception, "");

  }

  @Override
  public void handleInvalidURLError(Exception exception, String urlPath) {
    count++;    
  }

}
