package org.jboss.errai.bus.server;

import java.util.List;

import javax.inject.Inject;

import org.jboss.errai.bus.client.framework.RequestDispatcher;
import org.jboss.errai.bus.client.tests.support.TestSerializationRPCService;
import org.jboss.errai.bus.client.tests.support.TreeNodeContainer;
import org.jboss.errai.bus.server.annotations.Service;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@Service
public class TestSerializationRPCServiceImpl implements TestSerializationRPCService {
  private RequestDispatcher dispatcher;

  @Inject
  public TestSerializationRPCServiceImpl(RequestDispatcher dispatcher) {
    this.dispatcher = dispatcher;
  }

  @Override
  public List<TreeNodeContainer> acceptTreeNodeContainers(List<TreeNodeContainer> listOfContainers) {
    int count = 0;
    for (TreeNodeContainer tc : listOfContainers) {
      System.out.println(tc.toString());
      count++;
    }

    return listOfContainers;
  }
}
