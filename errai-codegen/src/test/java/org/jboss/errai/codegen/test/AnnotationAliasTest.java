package org.jboss.errai.codegen.test;

import com.google.common.base.Function;
import com.sun.tools.javac.util.List;
import junit.framework.TestCase;
import org.jboss.errai.common.client.api.annotations.Alias;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.junit.Test;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Collection;

import static com.google.common.collect.Collections2.transform;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author edewit@redhat.com
 */
public class AnnotationAliasTest extends TestCase {

  @Test
  public void testFindAliasAnnotation() {
    //given
    final MetaClass metaClass = MetaClassFactory.get(Triangle.class);

    //when
    final Annotation[] annotations = metaClass.getAnnotations();

    //then
    assertNotNull(annotations);
    final Collection<Class<? extends Annotation>> annotationCollection = transform(List.from(annotations),
            new Function<Annotation, Class<? extends Annotation>>() {
              @Nullable
              @Override
              public Class<? extends Annotation> apply(@Nullable Annotation input) {
                return input != null ? input.annotationType() : null;
              }
            });

    assertNotNull(annotationCollection);
    assertFalse("Collection should contain the collected annotations", annotationCollection.isEmpty());
    assertTrue(annotationCollection.contains(Color.class));
    assertTrue(annotationCollection.contains(Red.class));
    assertTrue(annotationCollection.contains(Crimson.class));
  }

  @Alias
  @Red
  @Target(TYPE)
  @Retention(RUNTIME)
  public static @interface Crimson {
  }

  @Target(TYPE)
  @Retention(RUNTIME)
  public static @interface Color {
    String value() default "";
  }

  @Alias
  @Color("red")
  @Target(TYPE)
  @Retention(RUNTIME)
  public static @interface Red {
  }

  @Crimson
  // -> @Red -> @Color
  public static class Triangle {
  }
}