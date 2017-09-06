/*
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.codegen.meta.impl.apt;

import org.jboss.errai.codegen.apt.test.ErraiAptTest;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.junit.Assert;
import org.junit.Test;

import javax.lang.model.type.TypeMirror;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class APTMethodTest extends ErraiAptTest {


  @Test
  public void testGetReturnTypeConcreteClass() {
    TypeMirror typeMirror = getTypeElement(TestConcreteClass.class).asType();
    MetaMethod[] methods = new APTClass(typeMirror).getMethods();

    Assert.assertEquals("java.lang.String", methods[11].getReturnType().toString());
  }

  @Test
  public void testGetReturnTypeConcreteInterface() {
    TypeMirror typeMirror = getTypeElement(TestConcreteInterface.class).asType();
    MetaMethod[] methods = new APTClass(typeMirror).getMethods();

    Assert.assertEquals("java.lang.String", methods[9].getReturnType().toString());
  }

  @Test
  public void testGetReturnTypeGenericInterface() {
    TypeMirror typeMirror = getTypeElement(TestGenericInterface.class).asType();
    MetaMethod[] methods = new APTClass(typeMirror).getDeclaredMethods();

    Assert.assertEquals(1, methods.length);
    Assert.assertEquals("T", methods[0].getReturnType().toString());
  }

}