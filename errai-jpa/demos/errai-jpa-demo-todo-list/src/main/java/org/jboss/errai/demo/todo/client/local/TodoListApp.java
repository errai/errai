package org.jboss.errai.demo.todo.client.local;

import java.util.Collections;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.jboss.errai.demo.todo.shared.TodoItem;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.client.container.ClientBeanManager;
import org.jboss.errai.jpa.sync.client.local.ClientSyncManager;
import org.jboss.errai.ui.client.widget.ListWidget;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;

@Templated("#main")
@EntryPoint
public class TodoListApp extends Composite {

  @Inject EntityManager em;
  @Inject ClientBeanManager bm;

  @Inject @DataField TextBox newItemBox;
  @Inject @DataField ListWidget<TodoItem, TodoItemWidget> itemContainer;
  @Inject @DataField Button archiveButton;
  @Inject @DataField Button syncButton;

  @PostConstruct
  public void init() {
    refreshItems();
  }

  private void refreshItems() {
    System.out.println("Todo List Demo: refreshItems()");
    TypedQuery<TodoItem> query = em.createNamedQuery("currentItems", TodoItem.class);
    itemContainer.setItems(query.getResultList());
  }

  void onItemChange(@Observes TodoItem item) {
    em.flush();
    refreshItems();
  }

  @EventHandler("newItemBox")
  void onNewItem(KeyDownEvent event) {
    if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER && !newItemBox.getText().equals("")) {
      TodoItem item = new TodoItem();
      item.setText(newItemBox.getText());
      em.persist(item);
      em.flush();
      newItemBox.setText("");
      refreshItems();
    }
  }

  @EventHandler("archiveButton")
  void archive(ClickEvent event) {
    TypedQuery<TodoItem> query = em.createNamedQuery("currentItems", TodoItem.class);
    for (TodoItem item : query.getResultList()) {
      if (item.isDone()) {
        item.setArchived(true);
      }
    }
    em.flush();
    refreshItems();
  }

  @Inject ClientSyncManager syncManager;
  @EventHandler("syncButton")
  void sync(ClickEvent event) {
    syncManager.coldSync("allItems", TodoItem.class, Collections.<String,Object>emptyMap());
    em.flush();
    refreshItems();
  }
}
