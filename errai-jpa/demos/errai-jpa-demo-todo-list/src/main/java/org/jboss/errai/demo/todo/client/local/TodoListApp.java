package org.jboss.errai.demo.todo.client.local;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.jboss.errai.bus.client.api.BusErrorCallback;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.demo.todo.shared.LoginService;
import org.jboss.errai.demo.todo.shared.TodoItem;
import org.jboss.errai.demo.todo.shared.User;
import org.jboss.errai.ioc.client.container.ClientBeanManager;
import org.jboss.errai.jpa.sync.client.local.ClientSyncManager;
import org.jboss.errai.jpa.sync.client.shared.SyncResponse;
import org.jboss.errai.ui.client.widget.ListWidget;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShowing;
import org.jboss.errai.ui.nav.client.local.PageState;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

@Templated("#main")
@Page(path="list")
public class TodoListApp extends Composite {

  @Inject private EntityManager em;
  @Inject private ClientBeanManager bm;
  @Inject private ClientSyncManager syncManager;

  private @PageState Long userId;
  private User user; // filled in by @PageShowing method by lookup on userId

  @Inject private @DataField TextBox newItemBox;
  @Inject private @DataField ListWidget<TodoItem, TodoItemWidget> itemContainer;

  @Inject private @DataField Button archiveButton;
  @Inject private @DataField Button syncButton;

  @Inject private @DataField Label errorLabel;

  @Inject private @DataField InlineLabel username;

  @Inject private TransitionTo<LoginPage> logoutTransition;
  @Inject private @DataField Anchor logoutLink;
  @Inject private Caller<LoginService> loginService;

  @PageShowing
  private void onPageShowing() {
    if (userId == null) {
      Window.alert("No user id specified. Please sign in again.");
      logout(null);
    }

    user = em.find(User.class, userId);
    if (user == null) {
      Window.alert("Please sign in again.");
      logout(null);
    }

    username.setText(user.getFullName());
    errorLabel.setVisible(false);
    refreshItems();
  }

  private void refreshItems() {
    System.out.println("Todo List Demo: refreshItems()");
    TypedQuery<TodoItem> query = em.createNamedQuery("currentItemsForUser", TodoItem.class);
    query.setParameter("user", user);
    itemContainer.setItems(query.getResultList());
  }

  void onItemChange(@Observes TodoItem item) {
    em.flush();
    refreshItems();
  }

  @EventHandler("newItemBox")
  void onNewItem(KeyDownEvent event) {
    if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER && !newItemBox.getText().trim().equals("")) {
      TodoItem item = new TodoItem();
      item.setUser(user);
      item.setText(newItemBox.getText());
      em.persist(item);
      em.flush();
      newItemBox.setText("");
      refreshItems();
    }
  }

  @EventHandler("archiveButton")
  void archive(ClickEvent event) {
    TypedQuery<TodoItem> query = em.createNamedQuery("currentItemsForUser", TodoItem.class);
    query.setParameter("user", user);
    for (TodoItem item : query.getResultList()) {
      if (item.isDone()) {
        item.setArchived(true);
      }
    }
    em.flush();
    refreshItems();
  }

  @EventHandler("syncButton")
  void sync(ClickEvent event) {
    Map<String,Object> params = new HashMap<String, Object>();
    params.put("user", user);
    syncManager.coldSync("allItemsForUser", TodoItem.class, params,
            new RemoteCallback<List<SyncResponse<TodoItem>>>() {
              @Override
              public void callback(List<SyncResponse<TodoItem>> response) {
                System.out.println("Got data sync complete event!");
                refreshItems();
              }
            },
            new BusErrorCallback() {
              @Override
              public boolean error(Message message, Throwable throwable) {
                errorLabel.setText("Sync failed: " + throwable);
                errorLabel.setVisible(true);
                return false;
              }
            });
    System.out.println("Initiated cold sync");
  }

  @EventHandler("logoutLink")
  void logout(ClickEvent event) {
    syncManager.clear();
    loginService.call().logOut();
    logoutTransition.go();
  }
}
