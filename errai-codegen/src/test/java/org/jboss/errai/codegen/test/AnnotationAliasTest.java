/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.codegen.test;

import static com.google.common.collect.Collections2.transform;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Collection;

import javax.annotation.Nullable;

import junit.framework.TestCase;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.common.client.api.annotations.Alias;
import org.junit.Test;

import com.google.common.base.Function;

/**
 * @author edewit@redhat.com
 */
public class AnnotationAliasTest extends TestCase {

  /*
   * This test and feature have been disabled because the feature was not being
   * used and there was a noticeable performance penalty during compilation.
   */

   @Test
   public void testFindAliasAnnotation() {
//   //given
//   final MetaClass metaClass = MetaClassFactory.get(Triangle.class);
//  
//   //when
//   final Annotation[] annotations = metaClass.getAnnotations();
//  
//   //then
//   assertNotNull(annotations);
//   final Collection<Class<? extends Annotation>> annotationCollection =
//   transform(Arrays.asList(annotations),
//   new Function<Annotation, Class<? extends Annotation>>() {
//   @Nullable
//   @Override
//   public Class<? extends Annotation> apply(@Nullable Annotation input) {
//   return input != null ? input.annotationType() : null;
//   }
//   });
//  
//   assertNotNull(annotationCollection);
//   assertFalse("Collection should contain the collected annotations",
//   annotationCollection.isEmpty());
//   assertTrue(annotationCollection.contains(Color.class));
//   assertTrue(annotationCollection.contains(Red.class));
//   assertTrue(annotationCollection.contains(Crimson.class));
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
