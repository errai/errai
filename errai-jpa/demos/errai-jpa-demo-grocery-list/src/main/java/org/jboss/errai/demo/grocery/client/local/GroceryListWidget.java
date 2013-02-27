package org.jboss.errai.demo.grocery.client.local;

import org.jboss.errai.demo.grocery.client.shared.Item;
import org.jboss.errai.ui.client.widget.ListWidget;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Singleton
public class GroceryListWidget extends ListWidget<Item, GroceryItemWidget> {

  @Inject
  EntityManager entityManager;

  private SortBy sortField = SortBy.DEPARTMENT;

  public enum SortBy {
    NAME(new Comparator<Item>() {
      @Override
      public int compare(Item one, Item two) {
        return one.getName().compareTo(two.getName());
      }
    }),
    RECENTLY_ADDED(new Comparator<Item>() {
      @Override
      public int compare(Item one, Item two) {
        return one.getAddedOn().compareTo(two.getAddedOn());
      }
    }),
    DEPARTMENT(new Comparator<Item>() {
      @Override
      public int compare(Item one, Item two) {
        return one.getDepartment().getName().compareTo(two.getDepartment().getName());
      }
    });

    private final Comparator<Item> itemComparator;

    SortBy(Comparator<Item> itemComparator) {
      this.itemComparator = itemComparator;
    }
  }

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
    TypedQuery<Item> query = entityManager.createNamedQuery("allItems", Item.class);
    List<Item> itemList = query.getResultList();

    Collections.sort(itemList, sortField.itemComparator);
    setItems(itemList);
  }

  @Override
  public Class<GroceryItemWidget> getItemWidgetType() {
    return GroceryItemWidget.class;
  }

  public void sortBy(SortBy field) {
    this.sortField = field;
  }
}
