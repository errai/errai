package org.jboss.errai.enterprise.client.cdi;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class CountConstraint<T> implements TestConstraint<T> {
  private final int expected;
  private int count;

  public CountConstraint(int expected) {
    this.expected = expected;
  }

  @Override
  public ConstraintCondition processConstraint(T obj) {
    if (++count == expected) {
      return ConstraintCondition.Run;
    }
    else if (count > expected) {
      return ConstraintCondition.Failure;
    }
    else {
      return ConstraintCondition.Defer;
    }
  }
}
