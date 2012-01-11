package org.jboss.errai.ioc.tests.wiring.client.res;

import org.jboss.errai.ioc.client.api.EntryPoint;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@EntryPoint @BQual
public class GenericServiceString implements GenericService<String> {
  @Override
  public String get() {
    return "Hello";
  }
}
