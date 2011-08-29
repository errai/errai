package org.jboss.errai.enterprise.client.cdi;

import com.sun.tools.internal.xjc.reader.Const;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface TestConstraint<T> {
  public ConstraintCondition processConstraint(T obj);
}
