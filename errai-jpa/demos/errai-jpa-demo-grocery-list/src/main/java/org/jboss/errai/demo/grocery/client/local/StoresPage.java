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
import org.jboss.errai.ui.client.widget.ListWidget;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.common.collect.ImmutableMultimap;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;

@Dependent
@Templated("#root")
@Page
public class StoresPage extends Composite {

  // TODO eliminate this after making a bridge from JPA lifecycle events to CDI events
  private static StoresPage INSTANCE;

  @Inject private EntityManager em;

  @Inject TransitionTo<StorePage> toStorePage;
  @Inject @DataField ListWidget<Store, StoreWidget> storeList;

  @Inject @DataField Button addStoreButton;

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
    List<Store> allStores = em.createNamedQuery("allStores", Store.class).getResultList();
    storeList.setItems(allStores);
  }

  @EventHandler("addStoreButton")
  public void onStoreAddButtonClick(ClickEvent event) {
    Store newStore = new Store();
    em.persist(newStore);
    em.flush();
    toStorePage.go(ImmutableMultimap.of("id", String.valueOf(newStore.getId())));
  }
}
