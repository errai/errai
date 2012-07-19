package org.jboss.errai.demo.grocery.client.local;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.client.container.IOCBeanManager;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
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

  @Inject @DataField
  private HorizontalPanel popoverContent;

  @PostConstruct
  public void clientMain() {

    // show store form in popup when "+" button is pressed
    addStoreButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        final StoreForm storeForm = beanManager.lookupBean(StoreForm.class).getInstance();

        Element popover = Document.get().getElementById("addStorePopover");
        popover.getStyle().setDisplay(Display.BLOCK);
        popover.getStyle().setLeft(addStoreButton.getAbsoluteLeft() + addStoreButton.getOffsetWidth(), Unit.PX);
        popover.getStyle().setTop(
                addStoreButton.getAbsoluteTop() + addStoreButton.getOffsetHeight() / 2
                - popover.getOffsetHeight() / 2, Unit.PX);

        // TODO remove existing children of popoverContent

        popoverContent.add(storeForm);

        // hide store form when new store is saved
        storeForm.setAfterSaveAction(new Runnable() {
          @Override
          public void run() {
            Element el = Document.get().getElementById("addStorePopover");
            el.getStyle().setDisplay(Display.NONE);

            beanManager.destroyBean(storeForm);
          }
        });
      }
    });

    storesWidget.refreshFromDb();

    RootPanel.get().add(this);
  }
}
