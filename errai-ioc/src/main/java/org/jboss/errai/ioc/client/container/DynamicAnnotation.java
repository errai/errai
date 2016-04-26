package org.jboss.errai.ioc.client.container;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * A representation of an {@link Annotation} that was not present at
 * compile-time of this script.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface DynamicAnnotation extends Annotation {

  static DynamicAnnotation create(final String serialized) {
    return DynamicAnnotationImpl.create(serialized);
  }

  /**
   * Returns the fully qualified name of the annotation.
   *
   * @return fqcn of annotation, never null.
   */
  String getName();

  /**
   * Returns a map of member names to values for this annotation instance.
   *
   * @return map of members if present, otherwise an empty map.
   */
  Map<String, String> getMembers();
}
