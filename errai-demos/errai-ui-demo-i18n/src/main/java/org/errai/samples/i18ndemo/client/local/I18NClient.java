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

package org.errai.samples.i18ndemo.client.local;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.Bundle;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

@EntryPoint
@Bundle("i18n-client.json")
public class I18NClient {

  @Inject
  private RootPanel root;
  @Inject
  protected TemplatedWidget tw;
  @Inject
  protected TranslationService translationService;

  @PostConstruct
  public void post() {
    root.add(tw);
    root.add(new Label("Translation Service: " + translationService));
    root.add(new Label("Message 1: " + translationService.format(AppMessages.MESSAGE_1)));
    root.add(new Label("Message 2: " + translationService.format(AppMessages.MESSAGE_2, "TWO")));
    root.add(createLangAnchor("en_US"));
    root.add(createLangAnchor("da"));
    root.add(createLangAnchor("da_DA"));
    root.add(createLangAnchor("de_DE"));
    root.add(createLangAnchor("fr_FR"));
  }

  /**
   * @param lang
   */
  private Widget createLangAnchor(String lang) {
    FlowPanel fp = new FlowPanel();
    Anchor a = new Anchor(lang);
    a.setHref("?locale=" + lang);
    fp.add(a);
    return fp;
  }

}
