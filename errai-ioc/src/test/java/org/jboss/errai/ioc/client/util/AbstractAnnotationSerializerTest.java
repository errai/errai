/**
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ioc.client.util;

import org.junit.Test;

import java.lang.annotation.Annotation;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public abstract class AbstractAnnotationSerializerTest {

  @Test
  public void annotationWithNoProperties() throws Exception {
    final NoAttrAnnotation annotation = new NoAttrAnnotation() {
      @Override
      public Class<? extends Annotation> annotationType() {
        return NoAttrAnnotation.class;
      }
    };
    assertEquals(NoAttrAnnotation.class.getName(), serialize(annotation));
  }

  @Test
  public void annotationWithOneProperty() throws Exception {
    final OneAttrAnnotation annotation = new OneAttrAnnotation() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return OneAttrAnnotation.class;
      }

      @Override
      public int num() {
        return 1337;
      }
    };
    assertEquals(format("%s(num=%d)", OneAttrAnnotation.class.getName(), annotation.num()), serialize(annotation));
  }

  @Test
  public void annotationWithTwoProperties() throws Exception {
    final TwoAttrAnnotation annotation = new TwoAttrAnnotation() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return TwoAttrAnnotation.class;
      }

      @Override
      public String str() {
        return "foo";
      }

      @Override
      public int num() {
        return 1337;
      }
    };
    assertEquals(format("%s(num=%d,str=%s)", TwoAttrAnnotation.class.getName(), annotation.num(), annotation.str()),
            serialize(annotation));
  }

  @Test
  public void annotationWithSomeNonBindingProperties() throws Exception {
    final SomeNonBindingAttrAnnotation annotation = new SomeNonBindingAttrAnnotation() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return SomeNonBindingAttrAnnotation.class;
      }

      @Override
      public String str() {
        return "foo";
      }

      @Override
      public int num() {
        return 1337;
      }

      @Override
      public int nonBindingNum() {
        return -1;
      }
    };

    assertEquals(format("%s(num=%d,str=%s)", SomeNonBindingAttrAnnotation.class.getName(), annotation.num(),
            annotation.str()), serialize(annotation));
  }

  @Test
  public void annotationWithClassAttr() throws Exception {
    final ClassAttrAnnotation annotation = new ClassAttrAnnotation() {

      @Override
      public Class<?> clazz() {
        return String.class;
      }

      @Override
      public Class<? extends Annotation> annotationType() {
        return ClassAttrAnnotation.class;
      }
    };

    assertEquals(format("%s(clazz=%s)", ClassAttrAnnotation.class.getName(), String.class.getName()),
            serialize(annotation));
  }

  @Test
  public void annotationWithEnumAttr() throws Exception {
    final EnumAttrAnnotation annotation = new EnumAttrAnnotation() {

      @Override
      public TestEnum enun() {
        return TestEnum.A;
      }

      @Override
      public Class<? extends Annotation> annotationType() {
        return EnumAttrAnnotation.class;
      }
    };

    assertEquals(format("%s(enun=%s)", EnumAttrAnnotation.class.getName(), TestEnum.A.name()), serialize(annotation));
  }

  @Test
  public void annotationWithEmptyArrayAttr() throws Exception {
    final ArrayAttrAnnotation annotation = new ArrayAttrAnnotation() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return ArrayAttrAnnotation.class;
      }

      @Override
      public Class<?>[] classes() {
        return new Class[0];
      }
    };
    assertEquals(format("%s(classes=[])", ArrayAttrAnnotation.class.getName()), serialize(annotation));
  }

  @Test
  public void annotationWithOneElementsArrayAttr() throws Exception {
    final ArrayAttrAnnotation annotation = new ArrayAttrAnnotation() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return ArrayAttrAnnotation.class;
      }

      @Override
      public Class<?>[] classes() {
        return new Class[] { String.class };
      }
    };
    assertEquals(format("%s(classes=[%s])", ArrayAttrAnnotation.class.getName(), String.class.getName()),
            serialize(annotation));
  }

  @Test
  public void annotationWithTwoElementsArrayAttr() throws Exception {
    final ArrayAttrAnnotation annotation = new ArrayAttrAnnotation() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return ArrayAttrAnnotation.class;
      }

      @Override
      public Class<?>[] classes() {
        return new Class[] { String.class, Long.class };
      }
    };
    assertEquals(format("%s(classes=[%s, %s])", ArrayAttrAnnotation.class.getName(), String.class.getName(),
            Long.class.getName()), serialize(annotation));
  }

  public abstract String serialize(final Annotation annotation);

}
