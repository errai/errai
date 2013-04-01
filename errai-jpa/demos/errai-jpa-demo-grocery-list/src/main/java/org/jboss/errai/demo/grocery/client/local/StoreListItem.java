package org.jboss.errai.demo.grocery.client.local;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.demo.grocery.client.shared.Store;
import org.jboss.errai.ui.client.widget.HasModel;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * @author edewit@redhat.com
 */
@Dependent
@Templated
public class StoreListItem extends Composite implements HasModel<Store> {

  @AutoBound
  @Inject
  private DataBinder<Store> storeBinder;

  @Inject
  GroceryListWidget groceryListWidget;

  @Inject
  @DataField
  Anchor link;

  @Override
  public Store getModel() {
    return storeBinder.getModel();
  }

  @Override
  public void setModel(final Store model) {
    link.setText(model.getName());
    link.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        groceryListWidget.filterOn(model);
      }
    });
  }
}
