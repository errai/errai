package org.jboss.errai.demo.grocery.client.local;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.InitialState;
import org.jboss.errai.databinding.client.api.PropertyChangeEvent;
import org.jboss.errai.databinding.client.api.PropertyChangeHandler;
import org.jboss.errai.demo.grocery.client.local.convert.RelativeTimeConverter;
import org.jboss.errai.demo.grocery.client.local.convert.UsernameConverter;
import org.jboss.errai.demo.grocery.client.shared.Department;
import org.jboss.errai.demo.grocery.client.shared.Item;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

@Dependent
@Templated("#main")
public class ItemWidget extends Composite {

  @Inject private @AutoBound DataBinder<Item> itemBinder;
  @Inject private @Bound @DataField Label name;
  @Inject private @Bound @DataField Label comment;

  private @Bound(converter=RelativeTimeConverter.class) @DataField Element addedOn = DOM.createSpan();
  private @Bound(converter=UsernameConverter.class) @DataField Element addedBy = DOM.createSpan();

  @Inject private DataBinder<Department> deptBinder;
  @Inject private @DataField Label department;

  @PostConstruct
  void init() {

    itemBinder.getModel().setDepartment(new Department());
    deptBinder.setModel(itemBinder.getModel().getDepartment());
    deptBinder.bind(department, "name");

    // need to switch the databinder's model when the item's department reference changes
    itemBinder.addPropertyChangeHandler("department", new PropertyChangeHandler<Department>() {
      @Override
      public void onPropertyChange(PropertyChangeEvent<Department> event) {
        deptBinder.setModel(event.getNewValue());
      }
    });

    // TODO (ERRAI-382) convert these to a method with @EventHandler("this")
    addDomHandler(new MouseOverHandler() {
      @Override
      public void onMouseOver(MouseOverEvent event) {
        System.out.println("MouseOver " + itemBinder.getModel().getName());
        addStyleName("active");
      };
    }, MouseOverEvent.getType());

    addDomHandler(new MouseOutHandler() {
      @Override
      public void onMouseOut(MouseOutEvent event) {
        System.out.println("MouseOut " + itemBinder.getModel().getName());
        removeStyleName("active");
      };
    }, MouseOutEvent.getType());

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
  public Item setItem(Item item) {
    if (item.getDepartment() == null) {
      throw new NullPointerException("given item has null department; this is not allowed");
    }
    deptBinder.setModel(item.getDepartment(), InitialState.FROM_MODEL);
    return itemBinder.setModel(item, InitialState.FROM_MODEL);
  }
}
