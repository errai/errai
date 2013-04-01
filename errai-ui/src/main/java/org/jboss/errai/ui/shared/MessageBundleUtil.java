/*
 * Copyright 2012 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.ui.shared;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;


/**
 * Errai UI Runtime Utility for doing message bundle related stuff.
 *
 * @author eric.wittmann@redhat.com
 */
public final class MessageBundleUtil {

  private static RegExp LOCALE_IN_FILENAME_PATTERN = RegExp.compile("([^_]*)(_\\w\\w)?(_\\w\\w)?\\.json");

  /**
   * Private constructor.
   */
  private MessageBundleUtil() {
  }

  /**
   * Creates a dictionary from raw json data.
   * @param jsonData
   */
  public static void registerDictionary(String bundlePath, String jsonData) {
    MessageBundleLocaleInfo localeInfo = getLocaleFromBundleFilename(bundlePath);
    JSONMap data = JSONMap.create(jsonData);
    TranslationService.instance.register(data, localeInfo);
  }

  /**
   * Gets the locale information from the given bundle filename.  For example,
   * if the bundle filename is "myBundle_en_US.json" then this method should
   * return "en_US".
   * @param filename
   */
  public static MessageBundleLocaleInfo getLocaleFromBundleFilename(String filename) {
    MatchResult matcher = LOCALE_IN_FILENAME_PATTERN.exec(filename);
    if (matcher != null) {
      String lang = matcher.getGroup(2);
      if (lang != null)
        lang = lang.substring(1);
      String region = matcher.getGroup(3);
      if (region != null)
        region = region.substring(1);
      return new MessageBundleLocaleInfo(lang, region);
    } else {
      return null;
    }
  }

}
