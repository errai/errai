package org.jboss.errai.bus.server;

import org.jboss.errai.bus.client.TestSerializationRPCService;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.RequestDispatcher;
import org.jboss.errai.bus.client.tests.SerializationTests;
import org.jboss.errai.bus.client.tests.res.TreeNodeContainer;
import org.jboss.errai.bus.server.annotations.Service;

import javax.inject.Inject;
import java.util.List;

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
