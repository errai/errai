/*
 * Copyright (C) 2014 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.security.test.style.client.local.res;

import javax.inject.Inject;

import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;

@Templated
public class TemplatedStyleWidget extends Composite {

  @Inject
  @DataField
  private Anchor control;

  @Inject
  @DataField
  @RestrictedAccess
  private Anchor authenticatedAnchor;

  @Inject
  @DataField
  @RestrictedAccess(roles = "user")
  private Anchor userAnchor;

  @Inject
  @DataField
  @RestrictedAccess(roles = "admin")
  private Anchor adminAnchor;

  @Inject
  @DataField
  @RestrictedAccess(roles = { "user", "admin" })
  private Anchor userAdminAnchor;

  @Inject
  @DataField
  @CustomBinding
  @RestrictedAccess
  private Anchor customStyledUserAnchor;

  @Inject
  @DataField
  @RestrictedAccess(providers = { TestAnchorRoleProvider.class })
  private Anchor anchorWithProvidedRoles;

  public Anchor getUserAnchor() {
    return userAnchor;
  }

  public Anchor getAdminAnchor() {
    return adminAnchor;
  }

  public Anchor getUserAdminAnchor() {
    return userAdminAnchor;
  }

  public Anchor getControl() {
    return control;
  }

  public Anchor getAuthenticatedAnchor() {
    return authenticatedAnchor;
  }

  public Anchor getCustomStyledUserAnchor() {
    return customStyledUserAnchor;
  }

  @CustomBinding
  private void testBindingStyleUpdate(Style style) {
    style.setColor("red");
  }

  public Anchor getAnchorWithProvidedRoles() {
    return anchorWithProvidedRoles;
  }
}
