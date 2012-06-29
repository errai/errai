package org.jboss.errai.demo.grocery.client.local;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.jboss.errai.demo.grocery.client.shared.Store;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;

@Templated
@EntryPoint
public class GroceryListClient extends Composite {

  @Inject
  private EntityManager em;

  @Inject @DataField
  private StoreForm storeForm;

  @PostConstruct
  public void clientMain() {
    List<Store> allStores = em.createNamedQuery("allStores", Store.class).getResultList();
    for (Store s : allStores) {

    }

    RootPanel.get().add(storeForm);
  }
}
