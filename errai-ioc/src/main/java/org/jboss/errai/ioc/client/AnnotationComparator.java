package org.jboss.errai.ioc.client;

import java.lang.annotation.Annotation;

/**
 * A simple comparator interface used by the {@link QualifierEqualityFactory} generator to create comparators for
 * testing the equality of qualifiers at runtime.
 * <p>
 * NOTE: This interface is only for tested the attribute-equality of like-typed annotations. For instance, you can
 * only compare an annotation of type <tt>Foo</tt> with another annotation of type <tt>Foo</tt>. You cannot
 * compare the equality of <tt>Foo</tt> with <tt>Bar</tt>. Attempting to do so will simply result in a
 * <tt>ClassCastException</tt>.
 *
 * @author Mike Brock
 */
public interface AnnotationComparator<T extends Annotation> {
  /**
   * Tests the equality of two qualifiers. Does not accept null values.
   *
   * @param a1 an annotation to be compared. cannot be null.
   * @param a2 an annotation to be compared. cannot be null.
   * @return true if the annotations match.
   */
  public boolean isEqual(T a1, T a2);
}
