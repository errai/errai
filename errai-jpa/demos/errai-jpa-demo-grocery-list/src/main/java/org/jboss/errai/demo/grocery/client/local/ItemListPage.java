package org.jboss.errai.demo.grocery.client.local;

import java.util.Iterator;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.jboss.errai.demo.grocery.client.shared.Item;
import org.jboss.errai.demo.grocery.client.shared.qual.New;
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

  @Inject IOCBeanManager bm;
  @Inject EntityManager em;

  @Inject @DataField VerticalPanel listContainer;
  @Inject @DataField ItemForm newItemForm;

  @PostConstruct
  private void initInstance() {
    refreshFromDb();
    newItemForm.setAfterSaveAction(new Runnable() {
      @Override
      public void run() {
        newItemForm.setItem(new Item());
      }
    });
  }

  @SuppressWarnings("unused")
  private void onNewItem(@Observes @New Item i) {
    System.out.println("ItemListPage@" + System.identityHashCode(this) + " got new item: " + i);
    refreshFromDb();
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

    TypedQuery<Item> itemQuery = em.createNamedQuery("allItemsByName", Item.class);
    for (Item item : itemQuery.getResultList()) {
      ItemWidget itemWidget = bm.lookupBean(ItemWidget.class).newInstance();
      itemWidget.setItem(item);
      listContainer.add(itemWidget);
    }
  }
}
