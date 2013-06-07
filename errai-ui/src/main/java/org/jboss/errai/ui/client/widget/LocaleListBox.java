package org.jboss.errai.ui.client.widget;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.view.client.ProvidesKey;
import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.ui.shared.TemplateUtil;
import org.jboss.errai.ui.shared.api.Locale;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.io.IOException;
import java.util.Collection;

/**
 * @author edewit@redhat.com
 */
@Dependent
public class LocaleListBox extends ValueListBox<Locale> {
  @Inject
  LocaleSelector selector;

  public LocaleListBox() {
    super(new LocaleRenderer(), new LocaleProvidesKey());
  }

  @AfterInitialization
  public void init() {
    setAcceptableValues(selector.getSupportedLocales());
    addValueChangeHandler(new ValueChangeHandler<Locale>() {
      @Override
      public void onValueChange(ValueChangeEvent<Locale> event) {
        selector.select(event.getValue().getLocale());
      }
    });
  }

  private static class LocaleRenderer implements Renderer<Locale> {
    @Override
    public String render(Locale locale) {
      return locale.getLabel();
    }

    @Override
    public void render(Locale locale, Appendable appendable) throws IOException {
      appendable.append(render(locale));
    }
  }

  private static class LocaleProvidesKey implements ProvidesKey<Locale> {

    @Override
    public Object getKey(Locale item) {
      final String activeLocale = TemplateUtil.getTranslationService().getActiveLocale();
      String defaultLanguage = activeLocale != null ? activeLocale : LocaleSelector.DEFAULT;
      return item == null || item.getLocale() == null ? defaultLanguage : item.getLocale();
    }
  }
}
