package org.jboss.errai.demo.grocery.client.local;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.InitialState;
import org.jboss.errai.demo.grocery.client.local.convert.RelativeTimeConverter;
import org.jboss.errai.demo.grocery.client.local.convert.UsernameConverter;
import org.jboss.errai.demo.grocery.client.shared.Item;
import org.jboss.errai.ui.client.widget.HasModel;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

@Dependent
@Templated("#main")
public class GroceryItemWidget extends Composite implements HasModel<Item> {

  @Inject private @AutoBound DataBinder<Item> itemBinder;
  @Inject private @Bound @DataField Label name;
  @Inject private @Bound @DataField Label comment;
  @Inject private @Bound(property="department.name") @DataField Label department;
  
  private @Bound(converter=RelativeTimeConverter.class) @DataField
  final Element addedOn = DOM.createSpan();
  private @Bound(converter=UsernameConverter.class) @DataField
  final Element addedBy = DOM.createSpan();
  
  @EventHandler
  public void onMouseOver(MouseOverEvent event) {
    System.out.println("MouseOver " + itemBinder.getModel().getName());
    addStyleName("active");
  }
  
  @EventHandler
  public void onMouseOut(MouseOutEvent event) {
    System.out.println("MouseOut " + itemBinder.getModel().getName());
    removeStyleName("active");
  }

  /**
   * Changes the model object visualized by this class to the given one.
   *
   * @param item
   *          The item that should become the model of this class. Must not be
   *          null.
   * @return The proxied version of the given item object, for purposes of data
   *         binding. If you intend to make any changes to the state of the item
   *         after adding it to this widget, you must do so via this returned
   *         proxy. If you modify the item directly (rather than via the proxy)
   *         then this UI widget will not update.
   */
  @Override
  public void setModel(Item item) {
    System.out.println("ItemWidget: adopting model object " + System.identityHashCode(item));
    itemBinder.setModel(item, InitialState.FROM_MODEL);
  }

  @Override
  public Item getModel() {
    return itemBinder.getModel();
  }
}
