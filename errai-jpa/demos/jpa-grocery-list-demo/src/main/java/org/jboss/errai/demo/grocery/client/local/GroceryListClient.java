package org.jboss.errai.demo.grocery.client.local;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.jboss.errai.demo.grocery.client.shared.Store;
import org.jboss.errai.ioc.client.api.EntryPoint;
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
import com.google.gwt.user.client.ui.RootPanel;

@Templated("#main")
@EntryPoint
public class GroceryListClient extends Composite {

  @Inject
  private EntityManager em;

  @Inject @DataField
  private StoreForm storeForm;

  @Inject @DataField
  private StoresWidget storesWidget;

  @Inject @DataField
  private Button addStoreButton;

  @PostConstruct
  public void clientMain() {
    // TODO move this into StoresWidget
    List<Store> allStores = em.createNamedQuery("allStores", Store.class).getResultList();
    for (Store s : allStores) {
      storesWidget.addStore(s);
    }

    // show store form in popup when "+" button is pressed
    addStoreButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        Element el = Document.get().getElementById("addStorePopover");
        el.getStyle().setDisplay(Display.BLOCK);
        el.getStyle().setLeft(addStoreButton.getAbsoluteLeft() + addStoreButton.getOffsetWidth(), Unit.PX);
        el.getStyle().setTop(
                addStoreButton.getAbsoluteTop() + addStoreButton.getOffsetHeight() / 2
                - el.getOffsetHeight() / 2, Unit.PX);
      }
    });

    // hide store form when new store is saved
    storeForm.setAfterSaveAction(new Runnable() {

      @Override
      public void run() {
        Element el = Document.get().getElementById("addStorePopover");
        el.getStyle().setDisplay(Display.NONE);
      }
    });

    RootPanel.get().add(this);
  }
}
