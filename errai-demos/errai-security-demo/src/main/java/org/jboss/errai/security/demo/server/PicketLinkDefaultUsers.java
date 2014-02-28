package org.jboss.errai.security.demo.server;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.basic.Grant;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

@Singleton
@Startup
public class PicketLinkDefaultUsers {

  @Inject
  private PartitionManager partitionManager;

  /**
   * <p>Loads some users during the first construction.</p>
   */
  @PostConstruct
  public void create() {
    final IdentityManager identityManager = partitionManager.createIdentityManager();
    final RelationshipManager relationshipManager = partitionManager.createRelationshipManager();

    User john = new User("john");

    john.setEmail("john@doe.com");
    john.setFirstName("John");
    john.setLastName("Doe");

    User hacker = new User("hacker");

    hacker.setEmail("hacker@illegal.ru");
    hacker.setFirstName("Hacker");
    hacker.setLastName("anonymous");
    
    identityManager.add(john);
    identityManager.add(hacker);
    final Password defaultPassword = new Password("123");
    identityManager.updateCredential(john, defaultPassword);
    identityManager.updateCredential(hacker, defaultPassword);

    Role roleDeveloper = new Role("simple");
    Role roleAdmin = new Role("admin");

    identityManager.add(roleDeveloper);
    identityManager.add(roleAdmin);

    relationshipManager.add(new Grant(john, roleDeveloper));
    relationshipManager.add(new Grant(john, roleAdmin));
  }

}
