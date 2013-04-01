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

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

/**
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class TranslationService {

  public static TranslationService instance = null;
  private static String currentLocale = null;

  private Map<String, Map<String, String>> translations = new HashMap<String, Map<String, String>>();

  /**
   * Constructor.
   */
  public TranslationService() {
    instance = this;
  }

  @PostConstruct
  public void postConstruct() {
    System.out.println("Post Construct: " + this);
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
    String localeLookup = localeInfo.toString();
    if (localeLookup != null) {
      localeLookup = localeLookup.toLowerCase();
    }
    System.out.println("Registering translation data: " + localeLookup);
    Map<String, String> translation = get(localeLookup);
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
    String localeName = currentLocale();
    System.out.println("Translating key: " + translationKey + "  into locale: " + localeName);
    Map<String, String> translationData = get(localeName);
    // Try the most specific version first (e.g. en_US)
    if (translationData.containsKey(translationKey)) {
      return translationData.get(translationKey);
    }
    // Now try the lang-only version (e.g. en)
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

  /**
   * @return the currently configured locale
   */
  public static String currentLocale() {
    if (currentLocale == null) {
      String localeParam = com.google.gwt.user.client.Window.Location.getParameter("locale");
      if (localeParam == null || localeParam.trim().length() == 0) {
        localeParam = getBrowserLocale();
        if (localeParam != null) {
          if (localeParam.indexOf('-') != -1) {
            localeParam = localeParam.replace('-', '_');
          }
        }
      }
      if (localeParam == null) {
        localeParam = "default";
      }
      currentLocale = localeParam.toLowerCase();
    }
    return currentLocale;
  }

  /**
   * Gets the browser's configured locale.
   */
  public final static native String getBrowserLocale() /*-{
    if ($wnd.navigator.language) {
      return $wnd.navigator.language;
    }
    if ($wnd.navigator.userLanguage) {
      return $wnd.navigator.userLanguage;
    }
    if ($wnd.navigator.browserLanguage) {
      return $wnd.navigator.browserLanguage;
    }
    if ($wnd.navigator.systemLanguage) {
      return $wnd.navigator.systemLanguage;
    }
    return null;
  }-*/;

  /**
   * Forcibly set the current locale.  Mostly useful for testing.
   * @param locale
   */
  public final static void setCurrentLocale(String locale) {
    currentLocale = locale;
  }

}
