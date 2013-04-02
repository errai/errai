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
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

/**
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class TranslationService {

  private static final Logger logger = Logger.getLogger(TranslationService.class.getName());

  public static TranslationService instance = null;
  private static String currentLocale = null;

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
    String localeLookup = localeInfo.toString();
    if (localeLookup != null) {
      localeLookup = localeLookup.toLowerCase();
    }
    logger.fine("Registering translation data for locale: " + localeLookup);
    Map<String, String> translation = get(localeLookup);
    Set<String> keys = data.keys();
    for (String key : keys) {
      logger.fine("  Key: " + key);
      String value = data.get(key);
      logger.fine("  Value: " + key);
      translation.put(key, value);
    }
    logger.fine("Registered " + keys.size() + " translation keys.");
  }

  /**
   * Gets the translation for the given i18n translation key.
   * @param translationKey
   */
  public String getTranslation(String translationKey) {
    String localeName = currentLocale();
    logger.fine("Translating key: " + translationKey + "  into locale: " + localeName);
    Map<String, String> translationData = get(localeName);
    // Try the most specific version first (e.g. en_US)
    if (translationData.containsKey(translationKey)) {
      logger.fine("Translation found in locale map: " + localeName);
      return translationData.get(translationKey);
    }
    // Now try the lang-only version (e.g. en)
    if (localeName != null && localeName.contains("_")) {
      localeName = localeName.substring(0, localeName.indexOf('_'));
      translationData = get(localeName);
      if (translationData.containsKey(translationKey)) {
        logger.fine("Translation found in locale map: " + localeName);
        return translationData.get(translationKey);
      }
    }
    translationData = get(null);
    // Fall back to the root
    if (translationData.containsKey(translationKey)) {
      logger.fine("Translation found in *default* locale mapl.");
      return translationData.get(translationKey);
    }
    // Nothing?  Then return null.
    logger.fine("Translation not found in any locale map, leaving unchanged.");
    return null;
  }

  /**
   * @return the currently configured locale
   */
  public static String currentLocale() {
    if (currentLocale == null) {
      String locale = com.google.gwt.user.client.Window.Location.getParameter("locale");
      if (locale == null || locale.trim().length() == 0) {
        locale = getBrowserLocale();
        if (locale != null) {
          if (locale.indexOf('-') != -1) {
            locale = locale.replace('-', '_');
          }
        }
      }
      if (locale == null) {
        locale = "default";
      }
      currentLocale = locale.toLowerCase();
      logger.fine("Discovered the current locale (either via query string or navigator) of: " + currentLocale);
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
