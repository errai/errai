package org.jboss.errai.demo.grocery.client.local;

import org.jboss.errai.demo.grocery.client.shared.Item;
import org.jboss.errai.ui.client.widget.ListWidget;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

public class GroceryListWidget extends ListWidget<Item, GroceryItemWidget> {

  @Inject
  EntityManager entityManager;

  private SortBy sortField = SortBy.NAME;

  public enum SortBy {
    NAME("i.name"),
    RECENTLY_ADDED("i.addedOn"),
    DEPARTMENT("department.name");

    private final String fieldName;

    SortBy(String fieldName) {
      this.fieldName = fieldName;
    }

    public String getFieldName() {
      return fieldName;
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
    query.setParameter("sortBy", sortField.getFieldName());
    setItems(query.getResultList());
  }

  @Override
  public Class<GroceryItemWidget> getItemWidgetType() {
    return GroceryItemWidget.class;
  }

  public void sortBy(SortBy field) {
    this.sortField = field;
  }
}
