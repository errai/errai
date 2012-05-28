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

  public static boolean isEqual(Annotation a1, Annotation a2) {
    return factory.isEqual(a1, a2);
  }

  public static boolean isSameType(Annotation a1, Annotation a2) {
    return !(a1 == null || a2 == null) && a1.annotationType().equals(a2.annotationType());
  }

  public static int hashCodeOf(Annotation a1) {
    return factory.hashCodeOf(a1);
  }

  public static boolean containsAll(final Collection<Annotation> allOf, final Collection<Annotation> in) {
    if (allOf.isEmpty()) return true;

    final Map<String, Annotation> allOfMap = new HashMap<String, Annotation>();
    final Map<String, Annotation> inMap = new HashMap<String, Annotation>();

    for (Annotation a : allOf) {
      allOfMap.put(a.annotationType().getName(), a);
    }

    for (Annotation a : in) {
      inMap.put(a.annotationType().getName(), a);
    }

    if (!inMap.keySet().containsAll(allOfMap.keySet())) {
      return false;
    }

    for (Map.Entry<String, Annotation> entry : allOfMap.entrySet()) {
       if (!factory.isEqual(entry.getValue(), inMap.get(entry.getKey()))) {
         return false;
       }
    }

    return true;
  }

  static int hashValueFor(int i) {
    return i;
  }

  static int hashValueFor(boolean b) {
    return b ? 1 : 0;
  }

  static int hashValueFor(long l) {
    return (int) l;
  }

  static int hashValueFor(float f) {
    return (int) f * 1000;
  }

  static int hashValueFor(double d) {
    return (int) d * 1000;
  }

  static int hashValueFor(char c) {
    return (int) c;
  }

  static int hashValueFor(byte b) {
    return (int) b;
  }

  static int hashValueFor(short s) {
    return (int) s;
  }

  static int hashValueFor(Object o) {
    if (o == null) return -1;
    return o.hashCode();
  }
}
