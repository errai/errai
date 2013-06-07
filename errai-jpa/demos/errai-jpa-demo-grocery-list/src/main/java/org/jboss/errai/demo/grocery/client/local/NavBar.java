package org.jboss.errai.demo.grocery.client.local;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.ui.client.widget.ListWidget;
import org.jboss.errai.ui.client.widget.LocaleSelector;
import org.jboss.errai.ui.client.widget.OrderedList;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.Locale;
import org.jboss.errai.ui.shared.api.annotations.*;

import javax.inject.Inject;
import java.util.ArrayList;

@Templated
public class NavBar extends Composite {

  @Inject @DataField Anchor home;
  @Inject @DataField Anchor items;
  @Inject @DataField Anchor stores;

  /**
   * Could have used the {@link org.jboss.errai.ui.client.widget.LocaleListBox} here instead, but
   * you can also create your own customised component with the LocaleSelector
   */
  @Inject
  private LocaleSelector selector;
  @Inject @DataField @OrderedList
  ListWidget<Locale, LanguageItem> language;

  @Inject TransitionTo<WelcomePage> homeTab;
  @Inject TransitionTo<StoresPage> storesTab;
  @Inject TransitionTo<ItemListPage> itemsTab;

  @AfterInitialization
  public void buildLangaugeList() {
    language.setItems(new ArrayList<Locale>(selector.getSupportedLocales()));
  }

  @EventHandler("home")
  public void onHomeButtonClick(ClickEvent e) {
    homeTab.go();
  }

  @EventHandler("items")
  public void onItemsButtonClick(ClickEvent e) {
    itemsTab.go();
  }

  @EventHandler("stores")
  public void onStoresButtonClick(ClickEvent e) {
    storesTab.go();
  }

}
