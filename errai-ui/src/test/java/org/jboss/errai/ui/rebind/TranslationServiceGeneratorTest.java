/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.ui.rebind;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.junit.Assert;
import org.junit.Test;

/**
 * Junit test for {@link TranslationService};
 * @author eric.wittmann@redhat.com
 */
public class TranslationServiceGeneratorTest {

  /**
   * Test method for {@link org.jboss.errai.ui.rebind.TranslationServiceGenerator#getLocaleFromBundlePath(java.lang.String)}.
   */
  @Test
  public void testGetLocaleFromBundleFilename() {
    Assert.assertEquals(null, TranslationServiceGenerator.getLocaleFromBundlePath("myBundle.json"));
    Assert.assertEquals(null, TranslationServiceGenerator.getLocaleFromBundlePath("myBundle_en_US.other"));

    Assert.assertEquals("en_US", TranslationServiceGenerator.getLocaleFromBundlePath("myBundle_en_US.json"));
    Assert.assertEquals("en_GB", TranslationServiceGenerator.getLocaleFromBundlePath("myBundle_en_GB.json"));
    Assert.assertEquals("en", TranslationServiceGenerator.getLocaleFromBundlePath("myBundle_en.json"));

    Assert.assertEquals("fr_CA", TranslationServiceGenerator.getLocaleFromBundlePath("Some-Other-Bundle_fr_CA.json"));
    Assert.assertEquals("fr_FR", TranslationServiceGenerator.getLocaleFromBundlePath("Some-Other-Bundle_fr_FR.json"));
    Assert.assertEquals("fr", TranslationServiceGenerator.getLocaleFromBundlePath("Some-Other-Bundle_fr.json"));

    Assert.assertEquals("en_US", TranslationServiceGenerator.getLocaleFromBundlePath("org/example/ui/client/local/myBundle_en_US.json"));
    Assert.assertEquals("en_GB", TranslationServiceGenerator.getLocaleFromBundlePath("org/example/ui/client/local/myBundle_en_GB.json"));
    Assert.assertEquals("en", TranslationServiceGenerator.getLocaleFromBundlePath("org/example/ui/client/local/myBundle_en.json"));
  }

  /**
   * Test method for {@link org.jboss.errai.ui.rebind.TranslationServiceGenerator#recordBundleKeys(java.util.Map, String, String)}.
   */
  @Test
  public void testRecordBundleKeys() {
    String jsonResourcePath = "org/jboss/errai/ui/test/i18n/client/I18nTemplateTest.json";
    Map<String, Set<String>> result = new HashMap<String, Set<String>>();
    TranslationServiceGenerator.recordBundleKeys(result, null, jsonResourcePath);
    Assert.assertEquals(1, result.keySet().size());
    Set<String> defaultKeys = result.get(null);
    Assert.assertEquals(9, defaultKeys.size());

    jsonResourcePath = "org/jboss/errai/ui/test/i18n/client/I18nTemplateTest_fr_FR.json";
    TranslationServiceGenerator.recordBundleKeys(result, "fr_FR", jsonResourcePath);
    Assert.assertEquals(2, result.keySet().size());
    defaultKeys = result.get(null);
    Assert.assertEquals(9, defaultKeys.size());
    Set<String> fr_FR_Keys = result.get("fr_FR");
    Assert.assertEquals(9, fr_FR_Keys.size());

    jsonResourcePath = "org/jboss/errai/ui/test/i18n/client/I18nTemplateTest_da.json";
    TranslationServiceGenerator.recordBundleKeys(result, "da", jsonResourcePath);
    Assert.assertEquals(3, result.keySet().size());
    defaultKeys = result.get(null);
    Assert.assertEquals(9, defaultKeys.size());
    fr_FR_Keys = result.get("fr_FR");
    Assert.assertEquals(9, fr_FR_Keys.size());
    Set<String> da_Keys = result.get("da");
    Assert.assertEquals(9, da_Keys.size());
  }

}
