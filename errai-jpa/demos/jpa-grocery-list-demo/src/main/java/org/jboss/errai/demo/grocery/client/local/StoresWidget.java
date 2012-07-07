package org.jboss.errai.demo.grocery.client.local;

import javax.enterprise.context.Dependent;

import org.jboss.errai.demo.grocery.client.shared.Store;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.TableElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.user.client.ui.Composite;

@Dependent
@Templated("GroceryListClient.html#storesWidget")
public class StoresWidget extends Composite {

  @DataField
  private TableElement table = Document.get().createTableElement();

  public void addStore(Store store) {
    TableRowElement row = table.insertRow(-1);
    row.insertCell(-1).setInnerText(store.getName());
  }
}
