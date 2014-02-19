package org.jboss.errai.codegen.meta;

import java.lang.annotation.Annotation;
import java.util.Set;

import org.jboss.errai.common.client.api.annotations.Alias;

/**
 * @author edewit@redhat.com
 */
public class AnnotationParser {

  public static Annotation[] parseAnnotations(Annotation[] annotations) {
    // Set<Annotation> result = new HashSet<Annotation>();
    // for (Annotation annotation : annotations) {
    // unwrap(result, annotation, annotation.annotationType().getAnnotations());
    // }
    //
    // return result.toArray(new Annotation[result.size()]);

    /*
     * TODO: Decide the fate of this @Alias feature. It is currently unused so
     * we have commented it out for the time being to improve performance of the
     * Bootstrapper genorater.
     */

    return annotations;
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
