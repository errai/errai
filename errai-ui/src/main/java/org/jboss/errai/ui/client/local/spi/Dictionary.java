package org.jboss.errai.ui.client.local.spi;

import java.util.*;

/**
 * Dictionary to keep the translations.
 * @author edewit@redhat.com
 */
public class Dictionary {
  private Map<String, Map<String, String>> translations = new HashMap<String, Map<String, String>>();

  public Map<String, String> get(String locale) {
    final Map<String, String> translation = translations.get(locale);
    if (translation != null) {
      return new HashMap<String, String>(translation);
    }
    return Collections.emptyMap();
  }

  public Collection<String> getSupportedLocals() {
    return new HashSet<String>(translations.keySet());
  }

  public void put(String locale, String key, String message) {
    getTranslation(locale).put(key, message);
  }

  private Map<String, String> getTranslation(String locale) {
    Map<String, String> translation = translations.get(locale);
    if (translation == null) {
      translation = new HashMap<String, String>();
      translations.put(locale, translation);
    }
    return translation;
  }
}
