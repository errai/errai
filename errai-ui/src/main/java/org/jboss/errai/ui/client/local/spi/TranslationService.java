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
package org.jboss.errai.ui.client.local.spi;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.async.AsyncBeanDef;
import org.jboss.errai.ioc.client.container.async.CreationalCallback;
import org.jboss.errai.ui.shared.DomVisit;
import org.jboss.errai.ui.shared.JSONMap;
import org.jboss.errai.ui.shared.TranslationDomRevisitor;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.jboss.errai.ui.shared.wrapper.ElementWrapper;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.Composite;

/**
 * A base class for a generated translation service that includes all of the translation visible at
 * compile time.
 * 
 * @author eric.wittmann@redhat.com
 * @author Max Barkley <mbarkley@redhat.com>
 */
public abstract class TranslationService {

  private static final Logger logger = Logger.getLogger(TranslationService.class.getName());
  private static String currentLocale = null;

  private Dictionary dictionary = new Dictionary();

  /**
   * @return true if the translation service is enabled/should be used
   */
  public boolean isEnabled() {
    return !dictionary.getSupportedLocals().isEmpty();
  }

  public Collection<String> getSupportedLocales() {
    return dictionary.getSupportedLocals();
  }

  /**
   * Registers the bundle with the translation service.
   * 
   * @param jsonData
   */
  protected void registerBundle(String jsonData, String locale) {
    JSONMap data = JSONMap.create(jsonData);
    register(data, locale);
  }

  /**
   * Registers some i18n data with the translation service. This is called for each discovered
   * bundle file.
   * 
   * @param data
   * @param locale
   */
  protected void register(JSONMap data, String locale) {
    if (locale != null) {
      locale = locale.toLowerCase();
    }
    logger.fine("Registering translation data for locale: " + locale);
    Set<String> keys = data.keys();
    for (String key : keys) {
      String value = data.get(key);
      dictionary.put(locale, key, value);
    }
    logger.fine("Registered " + keys.size() + " translation keys.");
  }

  /**
   * Gets the translation for the given i18n translation key.
   * 
   * @param translationKey
   */
  public String getTranslation(String translationKey) {
    String localeName = getActiveLocale();
    logger.fine("Translating key: " + translationKey + "  into locale: " + localeName);
    Map<String, String> translationData = dictionary.get(localeName);
    if (translationData.containsKey(translationKey)) {
      logger.fine("Translation found in locale map: " + localeName);
      return translationData.get(translationKey);
    }
    // Nothing? Then return null.
    logger.fine("Translation not found in any locale map, leaving unchanged.");
    return null;
  }

  public String getActiveLocale() {
    String localeName = currentLocale();
    if (!dictionary.get(localeName).isEmpty()) {
      return localeName;
    }
    if (localeName != null && localeName.contains("_")
            && !dictionary.get(localeName.substring(0, localeName.indexOf('_'))).isEmpty()) {
      return localeName.substring(0, localeName.indexOf('_'));
    }
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
   * Forcibly set the current locale and re-translate all instantiated {@link Templated} beans.
   * 
   * @param locale
   */
  public final static void setCurrentLocale(String locale) {
    setCurrentLocaleWithoutUpdate(locale);
    retranslateTemplatedBeans();
  }

  /**
   * Forcibly set the current locale but do not re-translate existing templated instances. Mostly
   * useful for testing.
   * 
   * @param locale
   */
  public final static void setCurrentLocaleWithoutUpdate(String locale) {
    currentLocale = locale;
  }

  /**
   * Re-translate displayed {@link Templated} beans to the current locale.
   */
  public static void retranslateTemplatedBeans() {
    // Translate DOM-attached templates
    DomVisit.revisit(new ElementWrapper(Document.get().getBody()), new TranslationDomRevisitor());

    // Translate DOM-detached Singleton templates
    for (AsyncBeanDef<Composite> beanDef : IOC.getAsyncBeanManager().lookupBeans(Composite.class)) {
      Class<? extends Annotation> scope = beanDef.getScope();
      if (scope != null
              && (scope.equals(ApplicationScoped.class)))
        beanDef.getInstance(new CreationalCallback<Composite>() {
          @Override
          public void callback(Composite beanInstance) {
            /*
             * Only translate parent-less widgets to avoid re-translating a single widget multiple
             * times (the call to revisit will traverse the whole subtree rooted at this widget).
             */
            if (beanInstance.getParent() == null && !beanInstance.isAttached())
              DomVisit.revisit(new ElementWrapper(beanInstance.getElement()), new TranslationDomRevisitor());
          }
        });
    }
  }
}
