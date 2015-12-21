/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageHidden;
import org.jboss.errai.ui.nav.client.local.PageHiding;
import org.jboss.errai.ui.nav.client.local.PageShowing;
import org.jboss.errai.ui.nav.client.local.PageShown;
import org.jboss.errai.ui.nav.client.local.PageState;
import org.jboss.errai.ui.shared.api.annotations.Templated;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Page
@Templated("NonCompositePage.html")
@ApplicationScoped
public class NonCompositePageWithLifecycleMethods {

  private int showing = 0, shown = 0, hiding = 0, hidden = 0;

  @PageState
  private String state;

  @PageShowing
  private void showing() {
    showing++;
  }

  @PageShown
  private void shown() {
    shown++;
  }

  @PageHiding
  private void hiding() {
    hiding++;
  }

  @PageHidden
  private void hidden() {
    hidden++;
  }

  public String getState() {
    return state;
  }

  public int getShowing() {
    return showing;
  }

  public int getShown() {
    return shown;
  }

  public int getHiding() {
    return hiding;
  }

  public int getHidden() {
    return hidden;
  }

}
