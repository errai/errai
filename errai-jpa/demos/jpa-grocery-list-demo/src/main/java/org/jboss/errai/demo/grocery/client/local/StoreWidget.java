package org.jboss.errai.demo.grocery.client.local;

import javax.inject.Inject;

import org.jboss.errai.demo.grocery.client.shared.Store;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableRowElement;

@Templated("GroceryListClient#store-row")
public class StoreWidget extends TableRowElement {

  private Store store;

  @Inject @DataField
  private TableCellElement name;

  public void setStore(Store store) {
    this.store = store;
    name.setInnerText(store.getName());
  }
}
