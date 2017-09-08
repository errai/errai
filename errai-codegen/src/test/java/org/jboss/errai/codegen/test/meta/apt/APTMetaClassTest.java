/*
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.codegen.test.meta.apt;

import com.google.testing.compile.CompilationRule;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.impl.apt.APTClass;
import org.jboss.errai.codegen.meta.impl.apt.APTClassUtil;
import org.jboss.errai.codegen.test.meta.AbstractMetaClassTest;
import org.junit.Before;
import org.junit.Rule;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class APTMetaClassTest extends AbstractMetaClassTest {

  @Rule
  public CompilationRule rule = new CompilationRule();

  @Before
  public void before() {
    APTClassUtil.init(rule.getTypes(), rule.getElements());
  }

  @Override
  protected MetaClass getMetaClassImpl(final Class<?> javaClass) {
    return new APTClass(getTypeMirror(javaClass));
  }

  @Override
  protected Class<? extends MetaClass> getTypeOfMetaClassBeingTested() {
    return APTClass.class;
  }

  private TypeMirror getTypeMirror(final Class<?> javaClass) {
    final TypeMirror mirror;
    if (javaClass.isArray()) {
      mirror = getArrayTypeMirror(javaClass);
    } else if (javaClass.isPrimitive()) {
      if ("void".equals(javaClass.getName())) {
        mirror = rule.getTypes().getNoType(TypeKind.VOID);
      } else {
        final TypeKind kind = TypeKind.valueOf(javaClass.getName().toUpperCase());
        mirror = rule.getTypes().getPrimitiveType(kind);
      }
    } else {
      mirror = rule.getElements().getTypeElement(javaClass.getName()).asType();
    }
    return mirror;
  }

  private TypeMirror getArrayTypeMirror(final Class<?> javaClass) {
    int arrayDepth = 0;
    Class<?> cur = javaClass;
    do {
      cur = cur.getComponentType();
      arrayDepth += 1;
    } while (cur.isArray());

    TypeMirror mirror = getTypeMirror(cur);
    do {
      mirror = rule.getTypes().getArrayType(mirror);
      arrayDepth -= 1;
    } while (arrayDepth > 0);

    return mirror;
  }
}
