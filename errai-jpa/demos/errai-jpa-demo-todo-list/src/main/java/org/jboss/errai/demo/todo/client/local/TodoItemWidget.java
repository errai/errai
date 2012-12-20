package org.jboss.errai.demo.todo.client.local;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.InitialState;
import org.jboss.errai.databinding.client.api.PropertyChangeEvent;
import org.jboss.errai.databinding.client.api.PropertyChangeHandler;
import org.jboss.errai.demo.todo.shared.TodoItem;
import org.jboss.errai.ui.client.widget.HasModel;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;

@Dependent
@Templated("TodoListApp.html#item")
public class TodoItemWidget extends Composite implements HasModel<TodoItem> {

  @Inject Event<TodoItem> itemChangedEvent;

  @Inject @AutoBound DataBinder<TodoItem> itemBinder;
  @Inject @Bound @DataField InlineLabel text;
  @Inject @Bound @DataField CheckBox done;

  @Override
  public void setModel(TodoItem item) {
    itemBinder.setModel(item, InitialState.FROM_MODEL);
    updateDoneStyle();
  }

  @Override
  public TodoItem getModel() {
    return itemBinder.getModel();
  }

  @PostConstruct
  void setup() {
    itemBinder.addPropertyChangeHandler(new PropertyChangeHandler<Object>() {
      @Override
      public void onPropertyChange(PropertyChangeEvent<Object> event) {
        TodoItem item = itemBinder.getModel();
        updateDoneStyle();
        itemChangedEvent.fire(item);
      }
    });
  }

  private void updateDoneStyle() {
    if (getModel().isDone()) {
      text.addStyleName("done");
    }
    else {
      text.removeStyleName("done");
    }
  }

  @EventHandler
  private void onClick(ClickEvent e) {
    TodoItem item = getModel();
    item.setDone(!item.isDone());
  }
}
