package org.jboss.errai.demo.grocery.client.local;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.jboss.errai.demo.grocery.client.shared.Item;
import org.jboss.errai.ioc.client.container.ClientBeanManager;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Composite;

@Dependent
@Templated("#main")
@Page
@ApplicationScoped
public class ItemListPage extends Composite {

  @Inject private ClientBeanManager bm;
  @Inject private EntityManager em;

  @Inject private @DataField GroceryListWidget listWidget;
  @Inject private @DataField ItemForm newItemForm;
  @Inject private @DataField SortWidget sortWidget;

  @PostConstruct
  private void initInstance() {

    // clear the item form after an item is saved
    newItemForm.setAfterSaveAction(new Runnable() {
      @Override
      public void run() {
        newItemForm.setItem(new Item());
      }
    });
  }

}
