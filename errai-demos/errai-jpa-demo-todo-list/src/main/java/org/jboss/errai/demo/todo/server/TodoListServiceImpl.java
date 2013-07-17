package org.jboss.errai.demo.todo.server;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.demo.todo.shared.SharedList;
import org.jboss.errai.demo.todo.shared.TodoItem;
import org.jboss.errai.demo.todo.shared.TodoListService;
import org.jboss.errai.security.shared.AuthenticationService;
import org.jboss.errai.security.shared.User;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

/**
 * @author edewit@redhat.com
 */
@Stateless @Service
public class TodoListServiceImpl implements TodoListService {
  @Inject private EntityManager entityManager;
  @Inject private AuthenticationService service;

  @Override
  public List<SharedList> getSharedTodoLists() {
    User currentUser = service.getUser();
    final TypedQuery<String> query = entityManager.createNamedQuery("sharedWithMe", String.class);
    query.setParameter("loginName", currentUser.getLoginName());

    final List<TodoItem> list = entityManager.createNamedQuery("allSharedItems", TodoItem.class)
            .setParameter("userIds", query.getResultList()).getResultList();

    final ArrayList<SharedList> sharedLists = new ArrayList<SharedList>();
    String userName = null;
    SharedList currentList = null;
    for (TodoItem todoItem : list) {
      if (!todoItem.getLoginName().equals(userName)) {
        currentList = new SharedList(todoItem.getLoginName());
        sharedLists.add(currentList);
        userName = todoItem.getLoginName();
      }

      currentList.getItems().add(todoItem);
    }

    return sharedLists;
  }
}
