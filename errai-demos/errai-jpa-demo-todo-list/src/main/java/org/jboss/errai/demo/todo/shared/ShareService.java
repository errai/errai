package org.jboss.errai.demo.todo.shared;

import org.jboss.errai.bus.server.annotations.Remote;

/**
 * @author edewit@redhat.com
 */
@Remote
public interface ShareService {


  /**
   * Share my list with another user via email.
   *
   * @param email the email of the user to share my list with
   */
  void share(String email) throws UnknownUserException;
}
