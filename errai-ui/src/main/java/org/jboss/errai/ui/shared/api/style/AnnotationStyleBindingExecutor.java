package org.jboss.errai.ui.shared.api.style;

import com.google.gwt.user.client.Element;

import java.lang.annotation.Annotation;

/**
 * @author edewit@redhat.com
 */
public abstract class AnnotationStyleBindingExecutor implements StyleBindingExecutor {
  public abstract void invokeBinding(Element element, Annotation annotation);

  @Override
  public void invokeBinding(Element element) {
    throw new IllegalArgumentException("should not be called");
  }
}
