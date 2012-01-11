package org.jboss.errai.ioc.tests.wiring.client.res;

import org.jboss.errai.ioc.client.api.EntryPoint;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@EntryPoint @AQual
public class GenericServiceInteger implements GenericService<Integer> {
  @Override
  public Integer get() {
    return 111;
  }
}
