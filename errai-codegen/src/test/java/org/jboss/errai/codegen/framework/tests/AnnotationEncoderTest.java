/*
 * Copyright 2011 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.codegen.framework.tests;

import java.lang.annotation.Target;

import javax.annotation.PostConstruct;

import org.jboss.errai.codegen.framework.AnnotationEncoder;
import org.jboss.errai.codegen.framework.tests.model.MyBean;
import org.jboss.errai.codegen.framework.tests.model.MyTestAnnotation;
import org.junit.Test;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class AnnotationEncoderTest extends AbstractStatementBuilderTest {

  @Test
  public void testEncodeAnnotation() {
    String enc = AnnotationEncoder.encode(PostConstruct.class.getAnnotation(Target.class)).generate(null);

    assertEquals("new java.lang.annotation.Target() { " +
            "public java.lang.annotation.ElementType[] value() { " +
            " return new java.lang.annotation.ElementType[] { " +
            " java.lang.annotation.ElementType.METHOD }; " +
            "} " +
            "public String toString() { " +
            " return \"@java.lang.annotation.Target(value=[METHOD])\"; " +
            "} " +
            "public Class annotationType() { " +
            " return java.lang.annotation.Target.class; " +
            "} " +
            "}", enc);
  }

  @Test
  public void testEncodeAnnotationWithMultipleProperties() {
    String enc = AnnotationEncoder.encode(MyBean.class.getAnnotation(MyTestAnnotation.class)).generate(null);

    assertEquals("new org.jboss.errai.codegen.framework.tests.model.MyTestAnnotation() { " +
            "public String toString() { " +
            "    return \"@org.jboss.errai.codegen.framework.tests.model.MyTestAnnotation(foo=barfoo, testEum=FOURTH)\"; " +
            "} " +
            "public Class annotationType() { " +
            "    return org.jboss.errai.codegen.framework.tests.model.MyTestAnnotation.class; " +
            "} " +
            "public String foo() { " +
            "    return \"barfoo\"; " +
            "} " +
            "public org.jboss.errai.codegen.framework.tests.model.TEnum testEum() { " +
            "    return org.jboss.errai.codegen.framework.tests.model.TEnum.FOURTH; " +
            "} " +

            "}", enc);
  }
}