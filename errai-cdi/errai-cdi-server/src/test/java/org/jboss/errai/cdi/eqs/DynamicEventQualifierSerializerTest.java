/**
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.cdi.eqs;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;

import java.lang.annotation.Annotation;

import javax.enterprise.util.Nonbinding;

import org.jboss.errai.cdi.server.DynamicEventQualifierSerializer;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the DynamicEventQualifierSerializer.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class DynamicEventQualifierSerializerTest {

  public @interface NoAttrAnno {
  }

  private static final NoAttrAnno noAttrAnno = new NoAttrAnno() {
    @Override
    public Class<? extends Annotation> annotationType() {
      return NoAttrAnno.class;
    }
  };

  public @interface OneAttrAnno {
    int num();
  }

  private static final OneAttrAnno oneAttrAnno = new OneAttrAnno() {

    @Override
    public Class<? extends Annotation> annotationType() {
      return OneAttrAnno.class;
    }

    @Override
    public int num() {
      return 1337;
    }
  };

  public @interface TwoAttrAnno {
    int num();
    String str();
  }

  private static final TwoAttrAnno twoAttrAnno = new TwoAttrAnno() {

    @Override
    public Class<? extends Annotation> annotationType() {
      return TwoAttrAnno.class;
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

  public @interface SomeNonBindingAttrAnno {
    int num();
    String str();
    @Nonbinding
    int nonBindingNum();
  }

  private static final SomeNonBindingAttrAnno someNonBindingAttrAnno = new SomeNonBindingAttrAnno() {

    @Override
    public Class<? extends Annotation> annotationType() {
      return SomeNonBindingAttrAnno.class;
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

  private DynamicEventQualifierSerializer eqs;

  @Before
  public void setup() {
    eqs = new DynamicEventQualifierSerializer();
  }

  @Test
  public void annotationWithNoProperties() throws Exception {
    assertEquals(eqs.serialize(noAttrAnno), NoAttrAnno.class.getName());
  }

  @Test
  public void annotationWithOneProperty() throws Exception {
    assertEquals(eqs.serialize(oneAttrAnno), format("%s(num=%d)", OneAttrAnno.class.getName(), oneAttrAnno.num()));
  }

  @Test
  public void annotationWithTwoProperties() throws Exception {
    assertEquals(eqs.serialize(twoAttrAnno), format("%s(num=%d,str=%s)", TwoAttrAnno.class.getName(),
            someNonBindingAttrAnno.num(), someNonBindingAttrAnno.str()));
  }

  @Test
  public void annotationWithSomeNonBindingProperties() throws Exception {
    assertEquals(eqs.serialize(someNonBindingAttrAnno),
            format("%s(num=%d,str=%s)", SomeNonBindingAttrAnno.class.getName(), twoAttrAnno.num(), twoAttrAnno.str()));
  }

}
