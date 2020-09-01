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

package org.jboss.errai.ui.nav.client.local.testpages;


import org.jboss.errai.ui.nav.client.local.HistoryToken;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageHiding;
import org.jboss.errai.ui.nav.client.local.PageShowing;
import org.jboss.errai.ui.nav.client.local.api.NavigationControl;

import javax.enterprise.context.ApplicationScoped;

import com.google.gwt.user.client.ui.SimplePanel;

@ApplicationScoped @Page
public class PageWithNavigationControl extends SimplePanel {

  public NavigationControl showControl;
  public NavigationControl hideControl;

  @PageShowing
  private void confirm(final HistoryToken historyToken, final NavigationControl control) {
    this.showControl = control;
  }
  
  @PageHiding
  private void confirm(final NavigationControl control) {
    this.hideControl = control;
  }

}
