package org.jboss.errai.bus.client;

import org.jboss.errai.bus.client.tests.res.TreeNodeContainer;
import org.jboss.errai.bus.server.annotations.Remote;

import java.util.List;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@Remote
public interface TestSerializationRPCService {
  List<TreeNodeContainer> acceptTreeNodeContainers(List<TreeNodeContainer> listOfContainers);
}
