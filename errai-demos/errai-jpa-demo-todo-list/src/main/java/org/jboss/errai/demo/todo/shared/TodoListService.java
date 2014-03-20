package org.jboss.errai.demo.todo.shared;

import java.util.List;

import org.jboss.errai.bus.server.annotations.Remote;
import org.jboss.errai.security.shared.api.annotation.RestrictAccess;

/**
 * @author edewit@redhat.com
 */
@Remote
public interface TodoListService {
  @RestrictAccess
  List<SharedList> getSharedTodoLists();
}
