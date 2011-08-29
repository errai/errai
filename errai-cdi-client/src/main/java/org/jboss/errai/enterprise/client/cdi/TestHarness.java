package org.jboss.errai.enterprise.client.cdi;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface TestHarness {
  public void registerUnexpected(UnexpectedEvent event);
}
