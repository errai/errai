package org.jboss.errai.codegen.meta;

import org.jboss.errai.common.client.api.annotations.Alias;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

/**
 * @author edewit@redhat.com
 */
public class AnnotationParser {

  public static Annotation[] parseAnnotations(Annotation[] annotations) {
    Set<Annotation> result = new HashSet<Annotation>();
    for (Annotation annotation : annotations) {
      unwrap(result, annotation, annotation.annotationType().getAnnotations());
    }

    return result.toArray(new Annotation[result.size()]);
  }

  private static void unwrap(Set<Annotation> result, Annotation parent, Annotation[] annotations) {
    result.add(parent);
    if (isAliasType(parent)) {
      for (Annotation annotation : annotations) {
        unwrap(result, annotation, annotation.annotationType().getAnnotations());
      }
    }
  }

  private static boolean isAliasType(Annotation parent) {
    final Annotation[] annotations = parent.annotationType().getAnnotations();
    for (Annotation annotation : annotations) {
      if (Alias.class.equals(annotation.annotationType())) {
        return true;
      }
    }
    return false;
  }

}
