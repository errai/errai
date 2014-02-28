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
import org.jboss.errai.demo.todo.shared.SharedList;
import org.jboss.errai.demo.todo.shared.TodoItem;
import org.jboss.errai.demo.todo.shared.TodoListService;
import org.jboss.errai.jpa.sync.client.local.ClientSyncManager;
import org.jboss.errai.jpa.sync.client.shared.SyncResponse;
import org.jboss.errai.security.client.local.identity.Identity;
import org.jboss.errai.security.shared.RequireAuthentication;
import org.jboss.errai.security.shared.User;
import org.jboss.errai.ui.client.widget.ListWidget;
import org.jboss.errai.ui.nav.client.local.DefaultPage;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShowing;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

@RequireAuthentication
@Templated("#main")
@Page(path="list", role = DefaultPage.class)
public class TodoListPage extends Composite {

  @Inject private EntityManager em;
  @Inject private ClientSyncManager syncManager;
  @Inject private Caller<TodoListService> todoListService;

  private User user; // filled in by @PageShowing method

  @Inject private @DataField TextBox newItemBox;
  @Inject private @DataField ListWidget<TodoItem, TodoItemWidget> itemContainer;

  @Inject private @DataField ListWidget<SharedList, SharedListWidget> sharedContainer;

  @Inject private @DataField Button archiveButton;
  @Inject private @DataField Button syncButton;
  @Inject private @DataField Button shareButton;

  @Inject private @DataField Label errorLabel;

  @Inject private @DataField InlineLabel username;

  @Inject private TransitionTo<LoginPage> logoutTransition;
  @Inject private TransitionTo<SharePage> sharePageTransition;
  @Inject private @DataField Anchor logoutLink;

  @Inject private Identity identity;

  @PageShowing
  private void onPageShowing() {
    identity.getUser(new AsyncCallback<User>() {
      @Override
      public void onSuccess(User result) {
        user = result;
        username.setText(user.getFullName());
        errorLabel.setVisible(false);
        refreshItems();

        todoListService.call(new RemoteCallback<List<SharedList>>() {
          @Override
          public void callback(List<SharedList> response) {
            sharedContainer.setItems(response);
          }
        }).getSharedTodoLists();
      }

      @Override
      public void onFailure(Throwable caught) {
        logoutTransition.go();
      }
    });
  }

  private void refreshItems() {
    System.out.println("Todo List Demo: refreshItems()");
    TypedQuery<TodoItem> query = em.createNamedQuery("currentItemsForUser", TodoItem.class);
    query.setParameter("userId", user.getLoginName());
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
      item.setLoginName(user.getLoginName());
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
    query.setParameter("userId", user.getLoginName());
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
    params.put("userId", user.getLoginName());
    syncManager.coldSync("allItemsForUser", TodoItem.class, params,
            new RemoteCallback<List<SyncResponse<TodoItem>>>() {
              @Override
              public void callback(List<SyncResponse<TodoItem>> response) {
                syncButton.setEnabled(true);
                System.out.println("Got data sync complete event!");
                refreshItems();
              }
            },
            new BusErrorCallback() {
              @Override
              public boolean error(Message message, Throwable throwable) {
                syncButton.setEnabled(true);
                errorLabel.setText("Sync failed: " + throwable);
                errorLabel.setVisible(true);
                return false;
              }
            });
    syncButton.setEnabled(false);
    System.out.println("Initiated cold sync");
  }

  @EventHandler("logoutLink")
  void logout(ClickEvent event) {
    syncManager.clear();
    identity.logout();
    logoutTransition.go();
  }

  @EventHandler("shareButton")
  void share(ClickEvent event) {
    sharePageTransition.go();
  }
}
