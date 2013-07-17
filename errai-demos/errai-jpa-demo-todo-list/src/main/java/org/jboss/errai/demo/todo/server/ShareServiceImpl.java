package org.jboss.errai.demo.todo.server;

import org.jboss.errai.demo.todo.shared.ShareService;
import org.jboss.errai.security.shared.AuthenticationService;
import org.jboss.errai.security.shared.User;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.SimpleRole;

import javax.inject.Inject;

/**
 * @author edewit@redhat.com
 */
public class ShareServiceImpl implements ShareService {

  @Inject AuthenticationService service;
  @Inject IdentityManager identityManager;

  @Override
  public void share(String email) {
    User currentUser = service.getUser();

    //if this was the real world we would sent a mail to the user that this todo list was just shared with him.
    //but this is _only_ a demo.

    final org.picketlink.idm.model.User user = identityManager.getUser(email);
  }
}
