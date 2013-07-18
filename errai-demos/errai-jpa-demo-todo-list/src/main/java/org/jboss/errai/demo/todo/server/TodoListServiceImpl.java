package org.jboss.errai.demo.todo.server;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.demo.todo.shared.SharedList;
import org.jboss.errai.demo.todo.shared.TodoItem;
import org.jboss.errai.demo.todo.shared.TodoListService;
import org.jboss.errai.demo.todo.shared.User;
import org.jboss.errai.security.shared.AuthenticationService;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author edewit@redhat.com
 */
@Stateless
@Service
public class TodoListServiceImpl implements TodoListService {
  @Inject
  private EntityManager entityManager;
  @Inject
  private AuthenticationService service;

  @Override
  public List<SharedList> getSharedTodoLists() {
    final ArrayList<SharedList> sharedLists = new ArrayList<SharedList>();
    org.jboss.errai.security.shared.User currentUser = service.getUser();
    final TypedQuery<User> query = entityManager.createNamedQuery("sharedWithMe", User.class);
    query.setParameter("loginName", currentUser.getLoginName());

    final Map<String, User> userNames = Maps.uniqueIndex(query.getResultList(), new Function<User, String>() {
      @Override
      public String apply(User input) {
        return input != null ? input.getLoginName() : "";
      }
    });

    final Set<String> sharedWithMe = userNames.keySet();

    if (!sharedWithMe.isEmpty()) {
      final List<TodoItem> list = entityManager.createNamedQuery("allSharedItems", TodoItem.class)
              .setParameter("userIds", sharedWithMe).getResultList();

      String userName = null;
      SharedList currentList = null;
      for (TodoItem todoItem : list) {
        if (!todoItem.getLoginName().equals(userName)) {
          currentList = new SharedList(userNames.get(todoItem.getLoginName()).getShortName());
          sharedLists.add(currentList);
          userName = todoItem.getLoginName();
        }

        currentList.getItems().add(todoItem);
      }
    }
    return sharedLists;
  }
}
