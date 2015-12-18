package org.jboss.errai.demo.todo.client.local;

import javax.annotation.PostConstruct;

import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.ClickEvent;

@Templated("TodoListPage.html#item")
public class LockedTodoItemWidget extends TodoItemWidget {

  @PostConstruct
  private void disableCheckbox() {
    done.setEnabled(false);
  }

  @Override
  protected void onClick(ClickEvent e) {
    // Override parent handler that marks item as complete/incomplete.
  }

}
