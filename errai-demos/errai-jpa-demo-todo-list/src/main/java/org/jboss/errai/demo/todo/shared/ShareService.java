package org.jboss.errai.demo.todo.shared;

/**
 * @author edewit@redhat.com
 */
public interface ShareService {


  /**
   * Share my list with another user via email.
   *
   * @param email the email of the user to share my list with
   */
  void share(String email);
}
