package org.jboss.errai.codegen.meta;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import org.jboss.errai.common.client.api.Assert;

/**
 * Contains shared functionality by all implementations of
 * {@link HasAnnotations}.
 * 
 * @author Christian Sadilek<csadilek@redhat.com>
 */
public abstract class AbstractHasAnnotations implements HasAnnotations {

  private Set<String> annotationPresentCache = null;

  /**
   * Checks if the provided annotation is present on this element (type, method,
   * field or parameter).
   * 
   * @param annotation
   *          the annotation type, must not be null.
   * @return true if annotation is present, otherwise false.
   */
  @Override
  public boolean isAnnotationPresent(final Class<? extends Annotation> annotation) {
    Assert.notNull(annotation);
    if (annotationPresentCache == null) {
      annotationPresentCache = new HashSet<String>();
      for (final Annotation a : getAnnotations()) {
        annotationPresentCache.add(a.annotationType().getName());
      }
    }

    return annotationPresentCache.contains(annotation.getName());
  }
}
