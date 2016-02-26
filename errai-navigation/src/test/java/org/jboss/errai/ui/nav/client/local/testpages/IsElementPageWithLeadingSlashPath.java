/**
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

package org.jboss.errai.ui.nav.client.local.testpages;

import javax.inject.Inject;

import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.HTMLDivElement;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.ui.nav.client.local.Page;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Page(path = IsElementPageWithLeadingSlashPath.IS_ELEMENT_PAGE)
public class IsElementPageWithLeadingSlashPath implements IsElement {

  public static final String IS_ELEMENT_PAGE = "/is-element-page";

  @Inject
  private HTMLDivElement div;

  @Override
  public HTMLElement getElement() {
    return div;
  }

}
