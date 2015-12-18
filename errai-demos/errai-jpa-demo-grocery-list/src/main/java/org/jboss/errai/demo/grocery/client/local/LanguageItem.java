package org.jboss.errai.demo.grocery.client.local;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.client.widget.HasModel;
import org.jboss.errai.ui.client.widget.LocaleSelector;
import org.jboss.errai.ui.nav.client.local.Navigation;
import org.jboss.errai.ui.shared.api.Locale;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
* @author edewit@redhat.com
*/
@Templated("ListItem.html")
public class LanguageItem extends Composite implements HasModel<Locale> {
  @Inject
  Navigation navigation;

  @AutoBound
  @Inject
  private DataBinder<Locale> localeDataBinder;

  @Inject
  private LocaleSelector selector;

  @Inject
  @DataField
  private Anchor link;

  @Override
  public Locale getModel() {
    return localeDataBinder.getModel();
  }

  @Override
  public void setModel(final Locale model) {
    link.setText(model.getLabel());
    link.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        selector.select(model.getLocale());
        navigation.goTo(navigation.getCurrentPage().name());
      }
    });
  }
}
