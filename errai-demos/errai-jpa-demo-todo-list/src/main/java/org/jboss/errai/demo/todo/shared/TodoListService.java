package org.jboss.errai.demo.todo.shared;

import org.jboss.errai.bus.server.annotations.Remote;
import org.jboss.errai.security.shared.RequireAuthentication;

import java.util.List;

/**
 * @author edewit@redhat.com
 */
@Remote
public interface TodoListService {
  @RequireAuthentication
  List<SharedList> getSharedTodoLists();
}
