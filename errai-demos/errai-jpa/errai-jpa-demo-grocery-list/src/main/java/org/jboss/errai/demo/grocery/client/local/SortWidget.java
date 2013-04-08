package org.jboss.errai.demo.grocery.client.local;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.demo.grocery.client.local.producer.StoreListProducer;
import org.jboss.errai.demo.grocery.client.shared.Store;
import org.jboss.errai.ui.client.widget.ListWidget;
import org.jboss.errai.ui.client.widget.OrderedList;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import static org.jboss.errai.demo.grocery.client.local.GroceryListWidget.SortBy.*;

/**
 * @author edewit@redhat.com
 */
@Templated("#main")
@SuppressWarnings({"UnusedDeclaration", "UnusedParameters"})
public class SortWidget extends Composite {
  @Inject
  GroceryListWidget groceryListWidget;

  @Inject
  StoreListProducer storeListProducer;

  @Inject @DataField
  Anchor name;

  @Inject @DataField
  Anchor recentlyAdded;

  @Inject @DataField
  Anchor department;

  @Inject @DataField
  Anchor storeAnchor;

  @Inject @DataField @OrderedList
  ListWidget<Store, StoreListItem> stores;

  @PostConstruct
  public void buildStoreFilter() {
    refresh();
  }

  public void storeListChanged(@Observes StoreListProducer.StoreChangedEvent event) {
    refresh();
  }

  private void refresh() {
    stores.setItems(storeListProducer.getStoresSortedOnDistance());
  }
  @EventHandler("name")
  public void onNameItemClick(ClickEvent e) {
    changeSortOrder(NAME);
  }

  @EventHandler("recentlyAdded")
  public void onRecentlyAddedItemClick(ClickEvent e) {
    changeSortOrder(RECENTLY_ADDED);
  }

  @EventHandler("department")
  public void onDepartmentItemClick(ClickEvent e) {
    changeSortOrder(DEPARTMENT);
  }

  @EventHandler("storeAnchor")
  public void onStoreItemClick(ClickEvent e) {
    changeSortOrder(STORE_LOCATION);
  }

  private void changeSortOrder(GroceryListWidget.SortBy order) {
    groceryListWidget.sortBy(order);
    groceryListWidget.refresh();
  }
}
