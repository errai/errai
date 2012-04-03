package org.jboss.errai.cdi.injection.client;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author Mike Brock
 */
@ApplicationScoped
public class EquHashCheckCycleB {
  @Inject
  EquHashCheckCycleA equHashCheckCycleA;

  private static int counter = 0;
  private String someFieldToCheckEqualityOn = String.valueOf(System.currentTimeMillis()) + (++counter);

  public EquHashCheckCycleA getEquHashCheckCycleA() {
    return equHashCheckCycleA;
  }

  public String getSomeFieldToCheckEqualityOn() {
    return someFieldToCheckEqualityOn;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof EquHashCheckCycleB)) return false;

    EquHashCheckCycleB that = (EquHashCheckCycleB) o;

    if (equHashCheckCycleA != null ? !equHashCheckCycleA.getSomeFieldToCheckEqualityOn().equals(that.getEquHashCheckCycleA().getSomeFieldToCheckEqualityOn()) : that.getEquHashCheckCycleA() != null)
      return false;
    if (someFieldToCheckEqualityOn != null ? !someFieldToCheckEqualityOn.equals(that.getSomeFieldToCheckEqualityOn()) : that.getSomeFieldToCheckEqualityOn() != null)
      return false;

    return true;
  }

  public int hashCode() {
    return 55555;
  }
}
