package org.jboss.errai.demo.todo.server;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.demo.todo.shared.AccessDeniedException;
import org.jboss.errai.jpa.sync.client.shared.DataSyncService;
import org.jboss.errai.jpa.sync.client.shared.SyncRequestOperation;
import org.jboss.errai.jpa.sync.client.shared.SyncResponse;
import org.jboss.errai.jpa.sync.client.shared.SyncableDataSet;
import org.jboss.errai.security.shared.AuthenticationService;
import org.jboss.errai.security.shared.User;

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
    if (dataSet.getQueryName().equals("allItemsForUser")) {
      User requestedUser = (User) dataSet.getParameters().get("user");
      if (!currentUser.getLoginName().equals(requestedUser.getLoginName())) {
        throw new AccessDeniedException("You don't have permission to sync user " + requestedUser.getLoginName());
      }
    }
    else {
      throw new IllegalArgumentException("You don't have permission to sync dataset " + dataSet.getQueryName().equals("userById"));
    }
    return dataSyncEjb.coldSync(dataSet, remoteResults);
  }
}
