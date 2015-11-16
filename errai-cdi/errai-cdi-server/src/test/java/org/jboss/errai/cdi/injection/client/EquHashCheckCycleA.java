package org.jboss.errai.cdi.injection.client;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author Mike Brock
 */
@ApplicationScoped
public class EquHashCheckCycleA {
  @Inject
  EquHashCheckCycleB equHashCheckCycleB;

  private static int counter = 0;
  private String someFieldToCheckEqualityOn = String.valueOf(System.currentTimeMillis()) + (++counter);

  public EquHashCheckCycleB getEquHashCheckCycleB() {
    return equHashCheckCycleB;
  }

  public String getSomeFieldToCheckEqualityOn() {
    return someFieldToCheckEqualityOn;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof EquHashCheckCycleA)) return false;

    EquHashCheckCycleA that = (EquHashCheckCycleA) o;
//
    if (equHashCheckCycleB != null ? !equHashCheckCycleB.getSomeFieldToCheckEqualityOn()
            .equals(that.getEquHashCheckCycleB().getSomeFieldToCheckEqualityOn()) : that.getEquHashCheckCycleB() != null)
      return false;
    if (someFieldToCheckEqualityOn != null ? !someFieldToCheckEqualityOn.equals(that.getSomeFieldToCheckEqualityOn()) : that.getSomeFieldToCheckEqualityOn() != null)
      return false;

    return true;
  }

  public int hashCode() {
    return 77777;
  }
}
