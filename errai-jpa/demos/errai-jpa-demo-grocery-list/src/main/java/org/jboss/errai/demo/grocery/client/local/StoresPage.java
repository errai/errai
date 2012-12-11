package org.jboss.errai.demo.grocery.client.local;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

import org.jboss.errai.demo.grocery.client.shared.Store;
import org.jboss.errai.ioc.client.container.IOCBeanManager;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.TableElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;

@Dependent
@Templated("#root")
@Page
public class StoresPage extends Composite {

  // XXX need a better way of getting at this instance from the StoreListener
  private static StoresPage INSTANCE;

  @Inject
  private IOCBeanManager beanManager;

  @Inject
  private EntityManager em;

  @DataField
  private TableElement table = Document.get().createTableElement();

  @Inject @DataField
  private Button addStoreButton;

  @PostConstruct
  private void initInstance() {
    INSTANCE = this;
    refreshFromDb();
  }

  @PreDestroy
  private void deInitInstance() {
    INSTANCE = null;
  }

  // TODO make a bridge from JPA lifecycle events to CDI events
  public static class StoreListener {
    @PostPersist @PostUpdate @PostRemove
    public void onStoreListChange(Store s) {
      if (INSTANCE != null) {
        INSTANCE.refreshFromDb();
      }
    }
  }

  public void refreshFromDb() {
    table.setInnerHTML("");
    List<Store> allStores = em.createNamedQuery("allStores", Store.class).getResultList();
    for (Store s : allStores) {
      addStore(s);
    }
  }

  private void addStore(Store store) {
    TableRowElement row = table.insertRow(-1);
    row.insertCell(-1).setInnerText(store.getName());
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
    storeForm.grabKeyboardFocus();

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
