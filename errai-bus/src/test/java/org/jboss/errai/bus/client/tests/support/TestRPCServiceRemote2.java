package org.jboss.errai.bus.client.tests.support;

import org.jboss.errai.bus.server.annotations.Remote;

import java.util.List;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@Remote
public interface TestRPCServiceRemote2 {
  public List<Long> heresALongList(List<Long> longList);
}
