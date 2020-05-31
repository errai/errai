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

import org.jboss.errai.ui.nav.client.local.*;

import com.google.gwt.user.client.ui.VerticalPanel;

@Page
public class PageWithLifecycleMethods extends VerticalPanel {

  @PageState private String state;

  public int beforeShowCallCount = 0;
  public int afterShowCallCount = 0;
  public int beforeHideCallCount = 0;
  public int afterHideCallCount = 0;
  public int afterUpdateCallCount = 0;

  public String stateWhenBeforeShowWasCalled;
  public String stateAfterUpdateWasCalled;

  @PageShowing
  private void beforeShow() {
    beforeShowCallCount++;
    stateWhenBeforeShowWasCalled = state;
  }

  @PageShown
  private void afterShown() {
	afterShowCallCount++;
  }

  @PageHiding
  private void beforeHide() {
    beforeHideCallCount++;
    state = "lastMinuteChange";
  }

  @PageHidden
  private void afterHide() {
	  afterHideCallCount++;
  }

  @PageUpdate
  private void afterUpdate() {
    afterUpdateCallCount++;
    stateAfterUpdateWasCalled = state;
  }
}
