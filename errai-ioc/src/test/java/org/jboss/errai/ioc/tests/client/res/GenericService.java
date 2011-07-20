package org.jboss.errai.ioc.tests.client.res;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface GenericService<T> {
  public T get();
}
