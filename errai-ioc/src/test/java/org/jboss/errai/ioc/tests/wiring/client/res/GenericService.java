package org.jboss.errai.ioc.tests.wiring.client.res;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface GenericService<T> {
  public T get();
}
