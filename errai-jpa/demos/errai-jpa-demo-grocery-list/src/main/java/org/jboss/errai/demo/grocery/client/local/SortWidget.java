package org.jboss.errai.demo.grocery.client.local;

import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.demo.grocery.client.local.producer.StoreListProducer;
import org.jboss.errai.demo.grocery.client.shared.Store;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
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

  @DataField
  Element stores = Document.get().createElement("ul");

  @PostConstruct
  public void buildStoreFilter() {
    for (final Store store : storeListProducer.getStoresSortedOnDistance()) {
      final AnchorElement anchor = Document.get().createAnchorElement();
      anchor.setInnerText(store.getName());
      Element li = Document.get().createElement("li");
      li.appendChild(anchor);
      stores.appendChild(li);
      Anchor anchorWidget = new Anchor(anchor) {
        @Override
        public HandlerRegistration addClickHandler(ClickHandler handler) {
          onAttach();
          return super.addClickHandler(handler);
        }
      };
      anchorWidget.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          groceryListWidget.filterOn(store);
        }
      });
    }
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
