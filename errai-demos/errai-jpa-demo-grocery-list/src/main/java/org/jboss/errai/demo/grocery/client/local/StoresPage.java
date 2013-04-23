package org.jboss.errai.demo.grocery.client.local;

import com.google.common.collect.ImmutableMultimap;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.demo.grocery.client.shared.Store;
import org.jboss.errai.ui.client.widget.ListWidget;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

import static org.jboss.errai.demo.grocery.client.local.producer.StoreListProducer.StoreChangedEvent;

@Dependent
@Templated("#root")
@Page
public class StoresPage extends Composite {
  @Inject private EntityManager em;

  @Inject TransitionTo<StorePage> toStorePage;
  @Inject @DataField ListWidget<Store, StoreWidget> storeList;

  @Inject @DataField Button addStoreButton;

  @PostConstruct
  private void initInstance() {
    List<Store> allStores = em.createNamedQuery("allStores", Store.class).getResultList();
    storeList.setItems(allStores);
  }

  public void storeListChanged(@Observes StoreChangedEvent event) {
    storeList.setItems(event.getStores());
  }

  @EventHandler("addStoreButton")
  public void onStoreAddButtonClick(ClickEvent event) {
    Store newStore = new Store();
    em.persist(newStore);
    em.flush();
    toStorePage.go(ImmutableMultimap.of("id", String.valueOf(newStore.getId())));
  }
}
