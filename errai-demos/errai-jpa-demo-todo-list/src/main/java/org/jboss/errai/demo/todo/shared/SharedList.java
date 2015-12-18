package org.jboss.errai.demo.todo.shared;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author edewit@redhat.com
 */
@Portable @Bindable
public class SharedList {
  private String userName;
  private List<TodoItem> items = new ArrayList<TodoItem>();

  public SharedList() {}

  public SharedList(@MapsTo("userName") String userName) {
    this.userName = userName;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public List<TodoItem> getItems() {
    return items;
  }

  public void setItems(List<TodoItem> items) {
    this.items = items;
  }
}
