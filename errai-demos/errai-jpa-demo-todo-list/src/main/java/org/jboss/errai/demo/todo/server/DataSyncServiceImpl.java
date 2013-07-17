package org.jboss.errai.demo.todo.server;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.security.auth.SimpleRole;
import org.jboss.errai.demo.todo.shared.AccessDeniedException;
import org.jboss.errai.jpa.sync.client.shared.DataSyncService;
import org.jboss.errai.jpa.sync.client.shared.SyncRequestOperation;
import org.jboss.errai.jpa.sync.client.shared.SyncResponse;
import org.jboss.errai.jpa.sync.client.shared.SyncableDataSet;
import org.jboss.errai.security.shared.AuthenticationService;
import org.jboss.errai.security.shared.User;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.query.IdentityQuery;

@ApplicationScoped @Service
public class DataSyncServiceImpl implements DataSyncService {

  @Inject private DataSyncEjb dataSyncEjb;
  @Inject private AuthenticationService service;

  @Override
  public <X> List<SyncResponse<X>> coldSync(SyncableDataSet<X> dataSet, List<SyncRequestOperation<X>> remoteResults) {
    User currentUser = service.getUser();
    System.out.println("DataSyncServiceImpl.currentUser is " + currentUser);
    if (currentUser == null) {
      throw new IllegalStateException("Nobody is logged in!");
    }

    // the userId that comes from the client can be tampered with and that is why we override it here
    dataSet.getParameters().put("userId", currentUser.getLoginName());

    if (dataSet.getQueryName().equals("allItemsForUser")) {
      List<String> userIds = new ArrayList<String>();
      dataSet.getParameters().put("userIds", userIds);
    }
    else {
      throw new IllegalArgumentException("You don't have permission to sync dataset");
    }
    return dataSyncEjb.coldSync(dataSet, remoteResults);
  }
}
