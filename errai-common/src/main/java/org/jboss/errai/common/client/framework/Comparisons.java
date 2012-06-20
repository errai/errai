package org.jboss.errai.common.client.framework;

/**
 * Non-instantiable utility methods for comparing two or more values.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class Comparisons {

  /**
   * Tests two potentially null object references for equality.
   * <p>
   * Specifically, this method returns true if and only if any of the
   * following conditions are met:
   * <ol>
   * <li>{@code o1 == o2} (satisfied if {@code o1} and {@code o2} are both null)
   * <li>{@code o1.equals(o2)}
   * </ol>
   *
   * @param o1
   *          One object to compare. Null is permitted.
   * @param o2
   *          The other object to compare. Null is permitted.
   * @return true if o1 and o2 are equal (either by reference equality or by
   *         Object.equals()); false otherwise
   */
  // MAINTAINERS BEWARE: Errai JPA generates code that uses this method.
  public static boolean nullSafeEquals(Object o1, Object o2) {
    return (o1 == o2) || (o1 != null && o1.equals(o2));
  }
}
