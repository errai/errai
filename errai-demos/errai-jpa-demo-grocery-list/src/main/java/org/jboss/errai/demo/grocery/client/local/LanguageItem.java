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

package org.jboss.errai.demo.grocery.client.local;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.client.widget.HasModel;
import org.jboss.errai.ui.client.widget.LocaleSelector;
import org.jboss.errai.ui.nav.client.local.Navigation;
import org.jboss.errai.ui.shared.api.Locale;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
* @author edewit@redhat.com
*/
@Templated("ListItem.html")
public class LanguageItem extends Composite implements HasModel<Locale> {
  @Inject
  Navigation navigation;

  @AutoBound
  @Inject
  private DataBinder<Locale> localeDataBinder;

  @Inject
  private LocaleSelector selector;

  @Inject
  @DataField
  private Anchor link;

  @Override
  public Locale getModel() {
    return localeDataBinder.getModel();
  }

  @Override
  public void setModel(final Locale model) {
    link.setText(model.getLabel());
    link.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        selector.select(model.getLocale());
        navigation.goTo(navigation.getCurrentPage().name());
      }
    });
  }
}
