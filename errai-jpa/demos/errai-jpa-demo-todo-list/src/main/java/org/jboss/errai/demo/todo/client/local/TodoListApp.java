package org.jboss.errai.demo.todo.client.local;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.jboss.errai.demo.todo.shared.TodoItem;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.client.container.IOCBeanManager;
import org.jboss.errai.ui.client.widget.ListWidget;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;

@Templated("#main")
@EntryPoint
public class TodoListApp extends Composite {

  @Inject EntityManager em;
  @Inject IOCBeanManager bm;

  @Inject @DataField TextBox newItemBox;
  @Inject @DataField ListWidget<TodoItem, ItemWidget> itemContainer;
  @Inject @DataField Button archiveButton;

  @PostConstruct
  public void init() {
    System.out.println("TodoListApp init started");
    RootPanel.get().add(this);
    refreshItems();
    System.out.println("TodoListApp init finished");
  }

  private void refreshItems() {
    TypedQuery<TodoItem> query = em.createNamedQuery("currentItems", TodoItem.class);
    itemContainer.setItems(query.getResultList());
  }

  void onItemChange(@Observes TodoItem item) {
    em.flush();
    refreshItems();
  }

  @EventHandler("newItemBox")
  void onNewItem(KeyDownEvent event) {
    if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
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
}
