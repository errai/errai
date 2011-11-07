package org.jboss.errai.bus.server;

import org.jboss.errai.bus.client.tests.support.TestRPCServiceRemote2;
import org.jboss.errai.bus.server.annotations.Service;

import java.util.List;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@Service
public class TestRPCService2 implements TestRPCServiceRemote2{
  @Override
  public List<Long> heresALongList(List<Long> longList) {
    return longList;
  }
}
