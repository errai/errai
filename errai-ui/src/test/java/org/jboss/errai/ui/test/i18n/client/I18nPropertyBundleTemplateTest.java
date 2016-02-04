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

package org.jboss.errai.ui.test.i18n.client;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.test.i18n.client.res.AppMessages;
import org.jboss.errai.ui.test.i18n.client.res.PropertyBundleTemplate;
import org.junit.Test;

import com.google.gwt.core.client.GWT;

public class I18nPropertyBundleTemplateTest extends AbstractErraiCDITest {

  /**
   * @see com.google.gwt.junit.client.GWTTestCase#getModuleName()
   */
  @Override
  public String getModuleName() {
    return getClass().getName().replaceAll("client.*$", "Test");
  }

  @Override
  protected void gwtSetUp() throws Exception {
    TranslationService.setCurrentLocaleWithoutUpdate("en");
    super.gwtSetUp();
  }

  @Test
  public void testTemplateTranslationWithPropertyBundle() {
    final PropertyBundleTemplate template = IOC.getBeanManager().lookupBean(PropertyBundleTemplate.class).getInstance();
    assertEquals("Hello", template.hello());

    TranslationService.setCurrentLocale("de");
    assertEquals("Hallo", template.hello());
  }

  @Test
  public void testTranslationKeyWithPropertyBundle() {
    // Looking up the TranslationService through the bean manager fails.
    final TranslationService translationService = GWT.create(TranslationService.class);
    assertEquals("Testing is fun!", translationService.format(AppMessages.MESSAGE));

    TranslationService.setCurrentLocale("de");
    assertEquals("Testen macht Spass!", translationService.format(AppMessages.MESSAGE));
  }

}
