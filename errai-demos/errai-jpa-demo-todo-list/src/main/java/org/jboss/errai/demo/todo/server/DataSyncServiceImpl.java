package org.jboss.errai.demo.todo.server;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.jpa.sync.client.shared.DataSyncService;
import org.jboss.errai.jpa.sync.client.shared.SyncRequestOperation;
import org.jboss.errai.jpa.sync.client.shared.SyncResponse;
import org.jboss.errai.jpa.sync.client.shared.SyncableDataSet;
import org.jboss.errai.security.shared.AuthenticationService;
import org.jboss.errai.security.shared.User;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

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
      // the userId that comes from the client can be tampered with and that is why we override it here
      // the server state is more secure.
      dataSet.getParameters().put("userId", currentUser.getLoginName());
    }
    else {
      throw new IllegalArgumentException("You don't have permission to sync dataset");
    }
    return dataSyncEjb.coldSync(dataSet, remoteResults);
  }
}
