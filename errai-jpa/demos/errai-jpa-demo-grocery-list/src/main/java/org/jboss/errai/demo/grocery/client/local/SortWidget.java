package org.jboss.errai.demo.grocery.client.local;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

import static org.jboss.errai.demo.grocery.client.local.GroceryListWidget.SortBy.*;

/**
 * @author edewit@redhat.com
 */
@Templated("#main")
public class SortWidget extends Composite {
  @Inject
  GroceryListWidget groceryListWidget;

  @Inject @DataField
  Anchor name;

  @Inject @DataField
  Anchor recentlyAdded;

  @Inject @DataField
  Anchor department;

  @Inject @DataField
  Anchor store;

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

  @EventHandler("store")
  public void onStoreItemClick(ClickEvent e) {
    changeSortOrder(STORE_LOCATION);
  }

  private void changeSortOrder(GroceryListWidget.SortBy order) {
    groceryListWidget.sortBy(order);
    groceryListWidget.refresh();
  }
}
