/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.demo.grocery.client.local;

import static com.google.gwt.dom.client.Style.Display.INLINE;
import static com.google.gwt.dom.client.Style.Display.NONE;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Navigation;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.nav.client.shared.NavigationEvent;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;

@Templated("GroceryListClient.html#footer")
public class Footer extends Composite {

  @Inject
  private @DataField
  SortWidget sortWidget;

  @Inject
  private Navigation navigation;

  @Inject
  @DataField
  private Button list;

  @Inject
  TransitionTo<StoresPage> storesTab;
  @Inject
  TransitionTo<ItemListPage> itemsTab;

  @EventHandler("list")
  public void onItemsButtonClick(ClickEvent e) {
    if (navigation.getCurrentPage().name().equals("StoresPage")) {
      itemsTab.go();
    }
    else {
      storesTab.go();
    }
  }

  @SuppressWarnings("unused")
  private void onPageSwitch(@Observes NavigationEvent event) {
    final Style style = sortWidget.getElement().getStyle();
    if ("ItemListPage".equals(event.getPageRequest().getPageName())) {
      style.setDisplay(INLINE);
    }
    else {
      style.setDisplay(NONE);
    }
  }
}
