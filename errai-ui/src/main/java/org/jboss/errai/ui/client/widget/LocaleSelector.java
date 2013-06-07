package org.jboss.errai.ui.client.widget;

import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.TemplateUtil;
import org.jboss.errai.ui.shared.api.Locale;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author edewit@redhat.com
 */
@Singleton
public class LocaleSelector {

  public static final String DEFAULT = "default";

  public Collection<Locale> getSupportedLocales() {
    Set<Locale> supportedLocales = new HashSet<Locale>();
    final TranslationService translationService = TemplateUtil.getTranslationService();
    final Collection<String> locales = translationService.getSupportedLocales();
    for (String localeKey : locales) {
      localeKey = localeKey == null ? DEFAULT : localeKey;
      supportedLocales.add(new Locale(localeKey, translationService.getTranslation(localeKey)));
    }

    return supportedLocales;
  }

  public void select(String locale) {
    TranslationService.setCurrentLocale(locale);
  }
}
