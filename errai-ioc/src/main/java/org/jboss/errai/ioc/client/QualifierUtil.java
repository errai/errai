package org.jboss.errai.ioc.client;

import com.google.gwt.core.client.GWT;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A utility class for testing the equality of qualifiers at runtime.
 *
 * @author Mike Brock
 */
public class QualifierUtil {
  private static final QualifierEqualityFactory factory = GWT.create(QualifierEqualityFactory.class);

  public static boolean isEqual(final Annotation a1, final Annotation a2) {
    return factory.isEqual(a1, a2);
  }

  public static boolean isSameType(final Annotation a1, final Annotation a2) {
    return !(a1 == null || a2 == null) && a1.annotationType().equals(a2.annotationType());
  }

  public static int hashCodeOf(final Annotation a1) {
    return factory.hashCodeOf(a1);
  }

  public static boolean containsAll(final Collection<Annotation> allOf, final Collection<Annotation> in) {
    if (allOf.isEmpty()) return true;

    final Map<String, Annotation> allOfMap = new HashMap<String, Annotation>();
    final Map<String, Annotation> inMap = new HashMap<String, Annotation>();

    for (final Annotation a : allOf) {
      allOfMap.put(a.annotationType().getName(), a);
    }

    for (final Annotation a : in) {
      inMap.put(a.annotationType().getName(), a);
    }

    if (!inMap.keySet().containsAll(allOfMap.keySet())) {
      return false;
    }

    for (final Map.Entry<String, Annotation> entry : allOfMap.entrySet()) {
       if (!factory.isEqual(entry.getValue(), inMap.get(entry.getKey()))) {
         return false;
       }
    }

    return true;
  }

  static int hashValueFor(final int i) {
    return i;
  }

  static int hashValueFor(final boolean b) {
    return b ? 1 : 0;
  }

  static int hashValueFor(final long l) {
    return (int) l;
  }

  static int hashValueFor(final float f) {
    return (int) f * 1000;
  }

  static int hashValueFor(final double d) {
    return (int) d * 1000;
  }

  static int hashValueFor(final char c) {
    return (int) c;
  }

  static int hashValueFor(final byte b) {
    return (int) b;
  }

  static int hashValueFor(final short s) {
    return (int) s;
  }

  static int hashValueFor(final Object o) {
    if (o == null) return -1;
    return o.hashCode();
  }
}
