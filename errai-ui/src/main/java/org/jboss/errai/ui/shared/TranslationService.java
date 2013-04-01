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
package org.jboss.errai.ui.shared;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import com.google.gwt.i18n.client.LocaleInfo;

/**
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class TranslationService {

  public static TranslationService instance = null;

  private Map<String, Map<String, String>> translations = new HashMap<String, Map<String, String>>();

  /**
   * Constructor.
   */
  public TranslationService() {
    instance = this;
  }

  /**
   * Gets a translation map for the given locale name (e.g. en_US).
   * @param localeName
   */
  protected Map<String, String> get(String localeName) {
    Map<String, String> rval = translations.get(localeName);
    if (rval == null) {
      rval = new HashMap<String, String>();
      translations.put(localeName, rval);
    }
    return rval;
  }

  /**
   * Registers some i18n data with the translation service.  This is called
   * for each discovered bundle file.
   * @param data
   * @param localeInfo
   */
  public void register(JSONMap data, MessageBundleLocaleInfo localeInfo) {
    System.out.println("Registering translation data: " + localeInfo.toString());
    Map<String, String> translation = get(localeInfo.toString());
    Set<String> keys = data.keys();
    for (String key : keys) {
      translation.put(key, data.get(key));
      System.out.println("   KEY: " + key + "  VALUE: " + data.get(key));
    }
  }

  /**
   * Gets the translation for the given i18n translation key.
   * @param translationKey
   */
  public String getTranslation(String translationKey) {
    LocaleInfo locale = LocaleInfo.getCurrentLocale();
    String localeName = locale.getLocaleName();
    System.out.println("Translating key: " + translationKey + "  into locale: " + localeName);
    Map<String, String> translationData = get(localeName);
    // Try the most specific version first (e.g. en_US)
    if (translationData.containsKey(translationKey)) {
      return translationData.get(translationKey);
    }
    // Now try the lang-only versoin (e.g. en)
    if (localeName != null && localeName.contains("_")) {
      localeName = localeName.substring(0, localeName.indexOf('_'));
      translationData = get(localeName);
      if (translationData.containsKey(translationKey)) {
        return translationData.get(translationKey);
      }
    }
    translationData = get(null);
    // Fall back to the root
    if (translationData.containsKey(translationKey)) {
      return translationData.get(translationKey);
    }
    // Nothing?  Then return null.
    return null;
  }

}
