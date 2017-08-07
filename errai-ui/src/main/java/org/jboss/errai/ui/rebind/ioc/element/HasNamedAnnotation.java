package org.jboss.errai.ui.rebind.ioc.element;

import org.jboss.errai.codegen.meta.HasAnnotations;
import org.jboss.errai.codegen.util.CDIAnnotationUtils;

import javax.inject.Named;
import java.lang.annotation.Annotation;

class HasNamedAnnotation implements HasAnnotations {

  private final Named named;

  HasNamedAnnotation(final String tagName) {
    this.named = new Named() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return Named.class;
      }

      @Override
      public String value() {
        return tagName;
      }

      @Override
      public int hashCode() {
        return CDIAnnotationUtils.hashCode(this);
      }

      @Override
      public String toString() {
        return CDIAnnotationUtils.toString(this);
      }

      @Override
      public boolean equals(final Object obj) {
        return obj instanceof Named && CDIAnnotationUtils.equals(this, (Annotation) obj);
      }
    };
  }

  @Override
  public boolean isAnnotationPresent(final Class<? extends Annotation> annotation) {
    return Named.class.equals(annotation);
  }

  @Override
  public Annotation[] getAnnotations() {
    return new Annotation[] { named };
  }

  @SuppressWarnings("unchecked")
  @Override
  public <A extends Annotation> A getAnnotation(final Class<A> annotation) {
    if (isAnnotationPresent(annotation)) {
      return (A) named;
    } else {
      return null;
    }
  }
}
