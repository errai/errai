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

import java.util.ArrayList;

import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.ui.client.widget.ListWidget;
import org.jboss.errai.ui.client.widget.LocaleSelector;
import org.jboss.errai.ui.client.widget.OrderedList;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.Locale;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;

@Templated
public class NavBar extends Composite {

  @Inject @DataField Button info;

  /**
   * Could have used the {@link org.jboss.errai.ui.client.widget.LocaleListBox} here instead, but
   * you can also create your own customised component with the LocaleSelector
   */
  @Inject
  private LocaleSelector selector;
  @Inject @DataField @OrderedList
  ListWidget<Locale, LanguageItem> language;

  @Inject TransitionTo<WelcomePage> homeTab;

  @AfterInitialization
  public void buildLangaugeList() {
    language.setItems(new ArrayList<Locale>(selector.getSupportedLocales()));
  }

  @EventHandler("info")
  public void onHomeButtonClick(ClickEvent e) {
    homeTab.go();
  }
}
