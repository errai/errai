/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.demo.grocery.client.local;

import static java.util.Collections.sort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.jboss.errai.demo.grocery.client.local.producer.StoreListProducer;
import org.jboss.errai.demo.grocery.client.shared.Department;
import org.jboss.errai.demo.grocery.client.shared.GroceryList;
import org.jboss.errai.demo.grocery.client.shared.Item;
import org.jboss.errai.demo.grocery.client.shared.Store;
import org.jboss.errai.demo.grocery.client.shared.qual.New;
import org.jboss.errai.demo.grocery.client.shared.qual.Removed;
import org.jboss.errai.demo.grocery.client.shared.qual.Updated;
import org.jboss.errai.ui.client.widget.ListWidget;
import org.jboss.errai.ui.cordova.geofencing.GeoFencingEvent;
import org.jboss.errai.ui.nav.client.local.TransitionTo;

@Singleton
public class GroceryListWidget extends ListWidget<Item, GroceryItemWidget> {

  @Inject
  EntityManager entityManager;

  @Inject
  GroceryList groceryList;

  @Inject
  ItemStoreDistanceComparator distanceComparator;

  @Inject
  TransitionTo<ItemListPage> itemsTab;

  private SortBy sortField = SortBy.NAME;

  public enum SortBy {
    NAME(new Comparator<Item>() {
      @Override
      public int compare(Item one, Item two) {
        return one.getName().compareTo(two.getName());
      }
    }), RECENTLY_ADDED(new Comparator<Item>() {
      @Override
      public int compare(Item one, Item two) {
        return two.getAddedOn().compareTo(one.getAddedOn());
      }
    }), DEPARTMENT(new Comparator<Item>() {
      @Override
      public int compare(Item one, Item two) {
        return one.getDepartment().getName().compareTo(two.getDepartment().getName());
      }
    }), STORE_LOCATION(null);

    private final Comparator<Item> itemComparator;

    SortBy(Comparator<Item> itemComparator) {
      this.itemComparator = itemComparator;
    }
  }

  @SuppressWarnings("unused")
  private void onNewItem(@Observes @New Item item) {
    /*
     * XXX This is imprecise. We don't even know if the affected item is in the grocery list this widget wraps. This
     * should be finer-grained (different cases for new, updated, removed) or the GroceryList class could do something
     * fancy.
     */
    System.out.println("Observed new item");
    this.getValue().add(item);
  }

  @SuppressWarnings("unused")
  private void onEditItem(@Observes @Updated Item item) {
    System.out.println("Observed updated item");
  }

  @SuppressWarnings("unused")
  private void onDeleteItem(@Observes @Removed Item item) {
    System.out.println("Observed deleted item");
    this.getValue().remove(item);
  }

  public void refreshListWidget() {
    List<Item> itemList = getAllSortedItems();
    setItems(itemList);
  }

  private List<Item> getAllSortedItems() {
    List<Item> itemList = getAllItems();

    if (sortField != SortBy.STORE_LOCATION) {
      sort(itemList, sortField.itemComparator);
    }
    else {
      sort(itemList, distanceComparator);
    }

    return itemList;

  }

  @PostConstruct
  private void onInit() {
    List<Item> itemList = getAllSortedItems();
    groceryList.getItems().addAll(itemList);
    setItems(groceryList.getItems());

  }

  private List<Item> getAllItems() {
    TypedQuery<Item> query = entityManager.createNamedQuery("allItems", Item.class);
    return query.getResultList();
  }

  @Override
  public Class<GroceryItemWidget> getItemComponentType() {
    return GroceryItemWidget.class;
  }

  public void sortBy(SortBy field) {
    this.sortField = field;
  }

  public void filterOn(Store store) {
    List<Item> itemList = getAllItems();
    List<Item> filtered = new ArrayList<Item>(itemList.size());
    for (Department department : store.getDepartments()) {
      for (Item item : itemList) {
        if (item.getDepartment().getName().equals(department.getName())) {
          filtered.add(item);
        }
      }
    }
    setItems(filtered);
  }

  public void closeToStore(@Observes GeoFencingEvent event) {
    itemsTab.go();
    Store store = entityManager.find(Store.class, event.getRegionId());
    if (store != null) {
      filterOn(store);
    }
  }

  public static class ItemStoreDistanceComparator implements Comparator<Item> {
    @Inject
    StoreListProducer storeListProducer;

    @Override
    public int compare(Item one, Item two) {
      int itemOneIndex = 0;
      int itemTwoIndex = 0;

      List<Store> stores = storeListProducer.getStoresSortedOnDistance();
      for (int i = 0, storesSize = stores.size(); i < storesSize; i++) {
        Store store = stores.get(i);
        for (Department department : store.getDepartments()) {
          if (department.getName().equals(one.getDepartment().getName())) {
            itemOneIndex = i;
          }
          if (department.getName().equals(two.getDepartment().getName())) {
            itemTwoIndex = i;
          }
        }
      }

      return (itemOneIndex < itemTwoIndex ? -1 : (itemOneIndex == itemTwoIndex ? 0 : 1));
    }
  }
}
