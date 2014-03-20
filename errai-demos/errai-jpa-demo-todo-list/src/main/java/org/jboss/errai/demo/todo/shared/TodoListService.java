package org.jboss.errai.demo.todo.shared;

import java.util.List;

import org.jboss.errai.bus.server.annotations.Remote;
import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;

/**
 * @author edewit@redhat.com
 */
@Remote
public interface TodoListService {
  @RestrictedAccess
  List<SharedList> getSharedTodoLists();
}
