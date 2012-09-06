package org.jboss.errai.demo.grocery.client.local;

import java.util.Iterator;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.TypedQuery;

import org.jboss.errai.demo.grocery.client.shared.Item;
import org.jboss.errai.ioc.client.container.IOCBeanManager;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

@Dependent
@Templated("#main")
@Page
public class ItemListPage extends Composite {

  // XXX need a better way of getting at this instance from the ItemListener
  private static ItemListPage INSTANCE;

  @Inject IOCBeanManager bm;
  @Inject EntityManager em;

  @Inject @DataField VerticalPanel listContainer;
  @Inject @DataField ItemForm newItemForm;

  @SuppressWarnings("unused")
  @PostConstruct
  private void initInstance() {
    INSTANCE = this;
    refreshFromDb();
  }

  @SuppressWarnings("unused")
  @PreDestroy
  private void deInitInstance() {
    INSTANCE = null;
  }

  // in a word, this JPA listener stuff is "yuck."
  // TODO make a bridge from JPA lifecycle events to CDI events
  public static class ItemListener {
    @PostPersist @PostUpdate @PostRemove
    public void onStoreListChange(Item s) {
      if (INSTANCE != null) {
        INSTANCE.refreshFromDb();
      }
    }
  }

  @PostConstruct
  void refreshFromDb() {

    // clean up the old widgets before we add new ones
    // (this will eventually become a feature of the ErraiUI framework)
    Iterator<Widget> it = listContainer.iterator();
    while (it.hasNext()) {
      bm.destroyBean(it.next());
      it.remove();
    }

    TypedQuery<Item> itemQuery = em.createNamedQuery("allItems", Item.class);
    for (Item item : itemQuery.getResultList()) {
      ItemWidget itemWidget = bm.lookupBean(ItemWidget.class).newInstance();
      itemWidget.setItem(item);
      listContainer.add(itemWidget);
    }
  }
}
