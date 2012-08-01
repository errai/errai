package org.jboss.errai.demo.grocery.client.local;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.client.container.IOCBeanManager;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;

@Templated("#main")
@EntryPoint
public class GroceryListClient extends Composite {

  @Inject
  private IOCBeanManager beanManager;

  @Inject @DataField
  private StoresWidget storesWidget;

  @Inject @DataField
  private Button addStoreButton;

  @PostConstruct
  public void clientMain() {
    storesWidget.refreshFromDb();
    RootPanel.get().add(this);
  }

  /**
   * Shows the store form in a popup when the "+" button is pressed.
   *
   * @param event the click event (ignored)
   */
  @EventHandler("addStoreButton")
  public void onStoreAddButtonClick(ClickEvent event) {
    final StoreForm storeForm = beanManager.lookupBean(StoreForm.class).getInstance();
    final PopoverContainer popover = beanManager.lookupBean(PopoverContainer.class).getInstance();
    popover.setTitleHtml(new SafeHtmlBuilder().appendEscaped("New Store").toSafeHtml());
    popover.setBodyWidget(storeForm);
    popover.show(addStoreButton);

    // hide store form when new store is saved
    storeForm.setAfterSaveAction(new Runnable() {
      @Override
      public void run() {
        popover.hide();
        beanManager.destroyBean(popover);
        beanManager.destroyBean(storeForm);
      }
    });
  }
}
