package org.jboss.errai.ioc.client;

import java.lang.annotation.Annotation;

/**
 * @author Mike Brock
 */
public interface AnnotationComparator<T extends Annotation> {
  public boolean isEqual(T a1, T a2);
}
