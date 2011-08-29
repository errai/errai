package org.jboss.errai.enterprise.client.cdi;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public abstract class EventResponseCallback<T> {
  private TestConstraint<T> constraint;
  private TestHarness harness;

  protected EventResponseCallback() {
  }

  public void setConstraint(TestConstraint<T> constraint) {
    this.constraint = constraint;
  }

  public void setHarness(TestHarness harness) {
    this.harness = harness;
  }

  public void runConditionally(T obj) {
    switch (constraint.processConstraint(obj)) {
      case Run:
        run();
        break;
      case Defer:
        break;
      case Failure:
        harness.registerUnexpected(new UnexpectedEvent(null, "did not expect event: " + obj));
    }
  }

  public abstract void run();
}
