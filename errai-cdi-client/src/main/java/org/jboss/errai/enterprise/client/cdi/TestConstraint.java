package org.jboss.errai.enterprise.client.cdi;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface TestConstraint<T> {
  public ConstraintCondition processConstraint(T obj);
}
