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

package org.jboss.errai.codegen.framework.tests.meta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.tests.model.ObjectWithNested;
import org.jboss.errai.codegen.framework.tests.model.TestInterface;
import org.junit.Test;

/**
 * Epic team effort!
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 * @author Mike Brock <cbrock@redhat.com>
 */
public class MetaClassTest {

  @Test
  public void testInternalNameForOneDimensionalPrimitiveArray() {
   String internalName = MetaClassFactory.get(char[].class).getInternalName();
   assertEquals("Wrong internal name generated for one-dimensional primitive array",
       "[C", internalName);
  }
  
  @Test
  public void testInternalNameForOneDimensionalObjectArray() {
   String internalName = MetaClassFactory.get(String[].class).getInternalName();
   assertEquals("Wrong internal name generated for one-dimensional object array",
       "[Ljava/lang/String;", internalName);
  }
  
  @Test
  public void testInternalNameForMultiDimensionalPrimitiveArray() {
   String internalName = MetaClassFactory.get(char[][].class).getInternalName();
   assertEquals("Wrong internal name generated for multidimensional primitive array",
       "[[C", internalName);
  }
  
  @Test
  public void testInternalNameForMultiDimensionalObjectArray() {
   String internalName = MetaClassFactory.get(String[][].class).getInternalName();
   assertEquals("Wrong internal name generated for multidimensional object array",
       "[[Ljava/lang/String;", internalName);
  }

  @Test
  public void testIsAssignableFromComparisonForNested() {
    ObjectWithNested objectWithNested = new ObjectWithNested();

    MetaClass interfaceClass = MetaClassFactory.get(TestInterface.class);
    MetaClass metaClass = MetaClassFactory.get(objectWithNested.getMyNestedInterface().getClass());

    assertTrue("should be assignable", interfaceClass.isAssignableFrom(metaClass));
  }
}