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

package org.jboss.errai.codegen.gwt.test;

import com.google.gwt.core.ext.typeinfo.TypeOracle;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.impl.gwt.GWTClass;
import org.jboss.errai.codegen.test.meta.AbstractMetaClassTest;
import org.jboss.errai.codegen.test.meta.method.MetaMethodTest;
import org.jboss.errai.codegen.test.model.PrimitiveFieldContainer;

import java.io.File;

/**
 * The GWT implementation of the overall MetaClass test. Inherits all the tests
 * from AbstractMetaClassTest and runs them against GWTClass. Don't remove this
 * test! It actually does something!
 *
 * @author Mike Brock
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
public class GWTMethodTest extends MetaMethodTest {

  @Override
  protected MetaClass getMetaClass(final Class<?> clazz) {
    return GWTMetaClassTest.getMetaClass(clazz);
  }
}
