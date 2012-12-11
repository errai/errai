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

import java.util.logging.Logger;

import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.HasText;

/**
 * Errai UI Runtime Utility for handling internationalization tasks.
 *
 * @author eric.wittmann@redhat.com
 */
public final class InternationalizationUtil {

  private static final Logger logger = Logger.getLogger(InternationalizationUtil.class.getName());

  /**
   * Constructor.
   */
  private InternationalizationUtil() {
  }

  /**
   * Replace the text of the given target widget with the value found in the named
   * i18n bundle.
   */
  public static void i18nTextReplace(String enclosingType, String targetName, HasText target,
          String bundleName, String i18nKey, String defaultValue) {
    String i18nValue = getI18NValueFromBundle(enclosingType, targetName,
            target, bundleName, i18nKey, defaultValue);
    target.setText(i18nValue);
  }

  /**
   * Replace the html of the given target widget with the value found in the named
   * i18n bundle.
   */
  public static void i18nHtmlReplace(String enclosingType, String targetName, HasHTML target,
          String bundleName, String i18nKey, String defaultValue) {
    String i18nValue = getI18NValueFromBundle(enclosingType, targetName,
            target, bundleName, i18nKey, defaultValue);
    target.setHTML(i18nValue);
  }

  /**
   * Gets the i18n replacement value from the named bundle.
   * @param enclosingType
   * @param targetName
   * @param target
   * @param bundleName
   * @param i18nKey
   * @param defaultValue
   */
  protected static String getI18NValueFromBundle(String enclosingType,
          String targetName, HasText target, String bundleName, String i18nKey,
          String defaultValue) {
    if (target == null) {
      throw new IllegalStateException("Widget to be internationalized (field/param [" + targetName
              + "] in class [" + enclosingType
              + "]) was null. Did you forget to @Inject or initialize it?");
    }

    String i18nValue = null;
    try {
      Dictionary dictionary = Dictionary.getDictionary(bundleName);
      i18nValue = dictionary.get(i18nKey);
    } catch (RuntimeException e) {
      // The bundle didn't contain the key, or the bundle couldn't be found.
      if (defaultValue == null) {
        logger.severe(e.getMessage());
        throw e;
      }
      i18nValue = defaultValue;
    }
    return i18nValue;
  }

}
