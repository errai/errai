package org.jboss.errai.demo.grocery.client.local;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.errai.demo.grocery.client.shared.Store;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.dom.client.TableElement;
import com.google.gwt.user.client.ui.Composite;

@Templated("GroceryListClient#store-root")
public class StoresWidget extends Composite {

  @Inject
  private Instance<StoreWidget> storeWidgetInstance;

  @Inject @DataField
  private TableElement storesTable;

  public void addStore(Store store) {
    StoreWidget storeWidget = storeWidgetInstance.get();
    storeWidget.setStore(store);
//    getElement().
  }
}
