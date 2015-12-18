package org.jboss.errai.demo.todo.server;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.model.basic.Realm;

@Startup
@Singleton
public class PicketLinkStartup {
  
  @Inject
  private PartitionManager partitionManager;
  
  @PostConstruct
  public void workaround() {
    /*
     * This forces the partition manager to be loaded on app startup. Without
     * this, it is possible for a FailedAuthenticationException to leave
     * picketlink in a bad state where the default partition is no longer
     * accessible.
     */
    partitionManager.getPartition(Realm.class, Realm.DEFAULT_REALM);
  }

}
