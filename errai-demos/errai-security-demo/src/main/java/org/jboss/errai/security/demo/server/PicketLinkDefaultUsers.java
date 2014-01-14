package org.jboss.errai.security.demo.server;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

@Singleton
@Startup
public class PicketLinkDefaultUsers {


  @Inject
  private IdentityManager identityManager;

  /**
   * <p>Loads some users during the first construction.</p>
   */
  @PostConstruct
  public void create() {

    User john = new SimpleUser("john");

    john.setEmail("john@doe.com");
    john.setFirstName("John");
    john.setLastName("Doe");

    User hacker = new SimpleUser("hacker");

    hacker.setEmail("hacker@illegal.ru");
    hacker.setFirstName("Hacker");
    hacker.setLastName("anonymous");
    
    identityManager.add(john);
    identityManager.add(hacker);
    final Password defaultPassword = new Password("123");
    identityManager.updateCredential(john, defaultPassword);
    identityManager.updateCredential(hacker, defaultPassword);

    Role roleDeveloper = new SimpleRole("simple");
    Role roleAdmin = new SimpleRole("admin");

    identityManager.add(roleDeveloper);
    identityManager.add(roleAdmin);

    identityManager.grantRole(john, roleDeveloper);
    identityManager.grantRole(john, roleAdmin);

  }

}
