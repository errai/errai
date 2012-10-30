package org.jboss.errai.demo.grocery.client.local;

import java.util.Iterator;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.jboss.errai.demo.grocery.client.shared.GroceryList;
import org.jboss.errai.demo.grocery.client.shared.Item;
import org.jboss.errai.ioc.client.container.IOCBeanDef;
import org.jboss.errai.ioc.client.container.IOCBeanManager;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class GroceryListWidget extends VerticalPanel {

  @Inject private IOCBeanManager bm;

  @Inject private GroceryList model;

  @SuppressWarnings("unused")
  private void onModelChange(@Observes @Any Item gl) {
    /* XXX This is imprecise. We don't even know if the affected item is in the
     * grocery list this widget wraps. This should be finer-grained (different
     * cases for new, updated, removed) or the GroceryList class could do something fancy.
     */
    refresh();
  }

  @PostConstruct
  void refresh() {

    // clean up the old widgets before we add new ones
    // (this will eventually become a feature of the ErraiUI framework: ERRAI-375)
    Iterator<Widget> it = iterator();
    while (it.hasNext()) {
      bm.destroyBean(it.next());
      it.remove();
    }

    if (model == null) return;

    IOCBeanDef<ItemWidget> itemBeanDef = bm.lookupBean(ItemWidget.class);
    for (Item item : model.getItems()) {
      ItemWidget itemWidget = itemBeanDef.newInstance();
      itemWidget.setItem(item);
      add(itemWidget);
    }
  }
}
