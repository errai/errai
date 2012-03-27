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

package org.jboss.errai.codegen.tests.meta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.tests.model.ObjectWithNested;
import org.jboss.errai.codegen.tests.model.TestInterface;
import org.jboss.errai.codegen.tests.model.tree.Child;
import org.jboss.errai.codegen.tests.model.tree.Grandparent;
import org.jboss.errai.codegen.tests.model.tree.GrandparentInterface;
import org.jboss.errai.codegen.tests.model.tree.GrandparentSuperInterface;
import org.jboss.errai.codegen.tests.model.tree.IsolatedInterface;
import org.jboss.errai.codegen.tests.model.tree.Parent;
import org.jboss.errai.codegen.tests.model.tree.ParentInterface;
import org.jboss.errai.codegen.tests.model.tree.ParentSuperInterface1;
import org.jboss.errai.codegen.tests.model.tree.ParentSuperInterface2;
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

  @Test
  public void testParentIsAssignableFromChild() {
    // This test checks the valid case:
    // Parent example = new Child();

    MetaClass metaChild = MetaClassFactory.get(Child.class);
    MetaClass metaParent = MetaClassFactory.get(Parent.class);

    assertTrue(metaParent.isAssignableFrom(metaChild));
  }

  @Test
  public void testChildIsNotAssignableFromParent() {
    // This test checks the disallowed case:
    // Child child = new Parent();

    MetaClass metaChild = MetaClassFactory.get(Child.class);
    MetaClass metaParent = MetaClassFactory.get(Parent.class);

    assertFalse(metaChild.isAssignableFrom(metaParent));
  }

  @Test
  public void testGrandParentIsAssignableFromChild() {
    // This test checks the valid case:
    // Grandparent example = new Child();

    MetaClass metaChild = MetaClassFactory.get(Child.class);
    MetaClass metaGrandparent = MetaClassFactory.get(Grandparent.class);

    assertTrue(metaGrandparent.isAssignableFrom(metaChild));
  }

  @Test
  public void testChildIsNotAssignableFromGrandParent() {
    // This test checks the disallowed case:
    // Child child = new Grandparent();

    MetaClass metaChild = MetaClassFactory.get(Child.class);
    MetaClass metaGrandparent = MetaClassFactory.get(Grandparent.class);

    assertFalse(metaChild.isAssignableFrom(metaGrandparent));
  }

  /**
   * This is a regression test for ERRAI-238.
   */
  @Test
  public void testUncleIsAssignableFromChild() {
    // This test checks the valid case:
    // ParentInterface example = new Child();

    MetaClass metaChild = MetaClassFactory.get(Child.class);
    MetaClass metaUncle = MetaClassFactory.get(ParentInterface.class);

    assertTrue(metaUncle.isAssignableFrom(metaChild));
  }

  @Test
  public void testChildIsNotAssignableFromUncle() {
    // This test checks the disallowed case:
    // Child child = new ParentInterface() {};

    MetaClass metaChild = MetaClassFactory.get(Child.class);
    MetaClass metaUncle = MetaClassFactory.get(ParentInterface.class);

    assertFalse(metaChild.isAssignableFrom(metaUncle));
  }

  /**
   * This is a regression test for ERRAI-238.
   */
  @Test
  public void testUncleInLawIsAssignableFromChild() {
    // This test checks the valid case:
    // ParentSuperInterface1 example = new Child();

    MetaClass metaChild = MetaClassFactory.get(Child.class);
    MetaClass metaUncleInLaw = MetaClassFactory.get(ParentSuperInterface1.class);

    assertTrue(metaUncleInLaw.isAssignableFrom(metaChild));
  }

  @Test
  public void testChildIsNotAssignableFromUncleInLaw() {
    // This test checks the disallowed case:
    // Child child = new ParentSuperInterface1() {};

    MetaClass metaChild = MetaClassFactory.get(Child.class);
    MetaClass metaUncleInLaw = MetaClassFactory.get(ParentSuperInterface1.class);

    assertFalse(metaChild.isAssignableFrom(metaUncleInLaw));
  }

  /**
   * This is a regression test for ERRAI-238.
   */
  @Test
  public void testUncleInLaw2IsAssignableFromChild() {
    // This test checks the valid case:
    // ParentSuperInterface2 example = new Child();

    MetaClass metaChild = MetaClassFactory.get(Child.class);
    MetaClass metaUncleInLaw = MetaClassFactory.get(ParentSuperInterface2.class);

    assertTrue(metaUncleInLaw.isAssignableFrom(metaChild));
  }

  @Test
  public void testChildIsNotAssignableFromUncleInLaw2() {
    // This test checks the disallowed case:
    // Child child = new ParentSuperInterface2() {};

    MetaClass metaChild = MetaClassFactory.get(Child.class);
    MetaClass metaUncleInLaw = MetaClassFactory.get(ParentSuperInterface2.class);

    assertFalse(metaChild.isAssignableFrom(metaUncleInLaw));
  }

  /**
   * This is a regression test for ERRAI-238.
   */
  @Test
  public void testGreatUncleIsAssignableFromChild() {
    // This test checks the valid case:
    // GrandparentInterface example = new Child();

    MetaClass metaChild = MetaClassFactory.get(Child.class);
    MetaClass metaGreatUncle = MetaClassFactory.get(GrandparentInterface.class);

    assertTrue(metaGreatUncle.isAssignableFrom(metaChild));
  }

  @Test
  public void testChildIsNotAssignableFromGreatUncle() {
    // This test checks the disallowed case:
    // Child child = new GrandparentInterface() {};

    MetaClass metaChild = MetaClassFactory.get(Child.class);
    MetaClass metaGreatUncle = MetaClassFactory.get(GrandparentInterface.class);

    assertFalse(metaChild.isAssignableFrom(metaGreatUncle));
  }

  /**
   * This is a regression test for ERRAI-238.
   */
  @Test
  public void testGreatUncleInLawIsAssignableFromChild() {
    // This test checks the valid case:
    //GrandparentSuperInterface example = new Child();

    MetaClass metaChild = MetaClassFactory.get(Child.class);
    MetaClass metaGreatUncleInLaw = MetaClassFactory.get(GrandparentSuperInterface.class);

    assertTrue(metaGreatUncleInLaw.isAssignableFrom(metaChild));
  }

  @Test
  public void testChildIsNotAssignableFromGreatUncleInLaw() {
    // This test checks the disallowed case:
    // Child child = new GrandparentSuperInterface() {};

    MetaClass metaChild = MetaClassFactory.get(Child.class);
    MetaClass metaGreatUncleInLaw = MetaClassFactory.get(GrandparentSuperInterface.class);

    assertFalse(metaChild.isAssignableFrom(metaGreatUncleInLaw));
  }

  @Test
  public void testObjectIsAssignableFromChild() {
    // This test checks the valid case:
    //Object example = new Child();

    MetaClass metaChild = MetaClassFactory.get(Child.class);
    MetaClass metaObject = MetaClassFactory.get(Object.class);

    assertTrue(metaObject.isAssignableFrom(metaChild));
  }

  @Test
  public void testChildIsNotAssignableFromObject() {
    // This test checks the disallowed case:
    // Child child = new Object();

    MetaClass metaChild = MetaClassFactory.get(Child.class);
    MetaClass metaObject = MetaClassFactory.get(Object.class);

    assertFalse(metaChild.isAssignableFrom(metaObject));
  }

  @Test
  public void testObjectIsAssignableFromIsolatedInterface() {
    // This test checks the valid case:
    // Object example = new IsolatedInterface() {};

    MetaClass metaInterface = MetaClassFactory.get(IsolatedInterface.class);
    MetaClass metaObject = MetaClassFactory.get(Object.class);

    assertTrue(metaObject.isAssignableFrom(metaInterface));
  }

  @Test
  public void testIsolatedInterfaceIsNotAssignableFromObject() {
    // This test checks the disallowed case:
    // IsolatedInterface ii = new Object();

    MetaClass metaInterface = MetaClassFactory.get(IsolatedInterface.class);
    MetaClass metaObject = MetaClassFactory.get(Object.class);

    assertFalse(metaInterface.isAssignableFrom(metaObject));
  }
}