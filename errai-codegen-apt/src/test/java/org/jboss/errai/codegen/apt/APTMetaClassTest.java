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

package org.jboss.errai.codegen.apt;

import static org.jboss.errai.codegen.apt.APTClassUtil.elements;
import static org.jboss.errai.codegen.apt.APTClassUtil.types;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.jboss.errai.codegen.apt.util.InProcessorTestRunner;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.test.meta.AbstractMetaClassTest;
import org.junit.runner.RunWith;

/**
 * Uses a custom test runner that runs inside an annotation processor to use real APT mirror implementation.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@RunWith(InProcessorTestRunner.class)
public class APTMetaClassTest extends AbstractMetaClassTest {

  public void setTypes(final Types types) {
    APTClassUtil.setTypes(types);
  }

  public void setElements(final Elements elements) {
    APTClassUtil.setElements(elements);
  }

  @Override
  protected MetaClass getMetaClassImpl(final Class<?> javaClass) {
    final TypeMirror mirror = getTypeMirror(javaClass);

    return new APTClass(mirror);
  }

  private static TypeMirror getTypeMirror(final Class<?> javaClass) {
    final TypeMirror mirror;
    if (javaClass.isArray()) {
      mirror = getArrayTypeMirror(javaClass);
    }
    else if (javaClass.isPrimitive()) {
      if ("void".equals(javaClass.getName())) {
        mirror = types.getNoType(TypeKind.VOID);
      }
      else {
        final TypeKind kind = TypeKind.valueOf(javaClass.getName().toUpperCase());
        mirror = types.getPrimitiveType(kind);
      }
    }
    else {
      mirror = elements.getTypeElement(javaClass.getName()).asType();
    }
    return mirror;
  }

  private static TypeMirror getArrayTypeMirror(final Class<?> javaClass) {
    int arrayDepth = 0;
    Class<?> cur = javaClass;
    do {
      cur = cur.getComponentType();
      arrayDepth += 1;
    } while (cur.isArray());

    TypeMirror mirror = getTypeMirror(cur);
    do {
      mirror = types.getArrayType(mirror);
      arrayDepth -= 1;
    } while (arrayDepth > 0);

    return mirror;
  }

  @Override
  protected Class<? extends MetaClass> getTypeOfMetaClassBeingTested() {
    return APTClass.class;
  }

}
