package org.jboss.errai.demo.grocery.client.local;

import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

import org.jboss.errai.demo.grocery.client.shared.Store;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.TableElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.user.client.ui.Composite;

@Dependent
@Templated("GroceryListClient.html#storesWidget")
public class StoresWidget extends Composite {

  // XXX need a better way of getting at this instance from the StoreListener
  private static StoresWidget INSTANCE;

  @Inject
  private EntityManager em;

  @DataField
  private TableElement table = Document.get().createTableElement();

  public static class StoreListener {
    @PostPersist @PostUpdate @PostRemove
    public void onStoreListChange(Store s) {
      INSTANCE.refreshFromDb();
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
}
