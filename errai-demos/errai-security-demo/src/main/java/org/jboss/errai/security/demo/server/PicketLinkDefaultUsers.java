/*
 * Copyright (C) 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.security.demo.server;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.basic.Grant;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;

/**
 * This class loads on startup and sets up some PicketLink users. The
 * {@link PartitionManager} is used to create {@link IdentityManager} and
 * {@link RelationshipManager}. The {@link IdentityManager} creates {@link User
 * Users} and {@link Role Roles} and the {@link RelationshipManager} creates
 * relationships between them.
 */
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
