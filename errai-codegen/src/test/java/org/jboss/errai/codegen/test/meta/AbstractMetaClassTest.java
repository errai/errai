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

package org.jboss.errai.codegen.test.meta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.test.model.ObjectWithNested;
import org.jboss.errai.codegen.test.model.TestInterface;
import org.jboss.errai.codegen.test.model.tree.Child;
import org.jboss.errai.codegen.test.model.tree.Grandparent;
import org.jboss.errai.codegen.test.model.tree.GrandparentInterface;
import org.jboss.errai.codegen.test.model.tree.GrandparentSuperInterface;
import org.jboss.errai.codegen.test.model.tree.IsolatedInterface;
import org.jboss.errai.codegen.test.model.tree.Parent;
import org.jboss.errai.codegen.test.model.tree.ParentInterface;
import org.jboss.errai.codegen.test.model.tree.ParentSuperInterface1;
import org.jboss.errai.codegen.test.model.tree.ParentSuperInterface2;
import org.junit.Test;
import org.mvel2.util.NullType;

/**
 * Subclassable container for the test cases that guarantee an implementation of
 * MetaClass conforms to the general contract. Each implementation of MetaClass
 * should have a corresponding subclass of this test. This way, every
 * implementation of MetaClass will be subjected to the same set of tests.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 * @author Mike Brock <cbrock@redhat.com>
 */
public abstract class AbstractMetaClassTest {

  /**
   * Requests the MetaClass impl from the subclass and checks that it's the
   * expected subtype of MetaClass before returning it.
   *
   * @param javaClass
   * @return
   */
  private MetaClass getMetaClass(Class<?> javaClass) {
    MetaClass impl = getMetaClassImpl(javaClass);
    assertEquals(getTypeOfMetaClassBeingTested(), impl.getClass());
    return impl;
  }

  /**
   * Returns a MetaClass object--of the type being tested--that represents {@code javaClass}.
   *
   * @param javaClass The Java class type being requested.
   * @return an instance of the subtype of MetaClass that's being tested.
   */
  protected abstract MetaClass getMetaClassImpl(Class<?> javaClass);

  /**
   * Returns the type of MetaClass that will be returned from {@link #getMetaClassImpl(Class)}.
   */
  protected abstract Class<? extends MetaClass> getTypeOfMetaClassBeingTested();

  @Test
  public void testInternalNameForOneDimensionalPrimitiveArray() {
   String internalName = getMetaClass(char[].class).getInternalName();
   assertEquals("Wrong internal name generated for one-dimensional primitive array",
       "[C", internalName);
  }

  @Test
  public void testInternalNameForOneDimensionalObjectArray() {
   String internalName = getMetaClass(String[].class).getInternalName();
   assertEquals("Wrong internal name generated for one-dimensional object array",
       "[Ljava/lang/String;", internalName);
  }

  @Test
  public void testInternalNameForMultiDimensionalPrimitiveArray() {
   String internalName = getMetaClass(char[][].class).getInternalName();
   assertEquals("Wrong internal name generated for multidimensional primitive array",
       "[[C", internalName);
  }

  @Test
  public void testInternalNameForMultiDimensionalObjectArray() {
   String internalName = getMetaClass(String[][].class).getInternalName();
   assertEquals("Wrong internal name generated for multidimensional object array",
       "[[Ljava/lang/String;", internalName);
  }

  @Test
  public void testObjectIsAssignableFromNull() throws Exception {
	  // This test checks the valid case:
	  // Object example = null;

	  MetaClass metaObject = getMetaClass(Object.class);
	  MetaClass metaNull = getMetaClass(NullType.class);

	  assertTrue(metaObject.isAssignableFrom(metaNull));
  }

  @Test
  public void testChildIsAssignableFromNull() throws Exception {
	  // This test checks the valid case:
	  // Child example = null;

	  MetaClass metaChild = getMetaClass(Child.class);
	  MetaClass metaNull = getMetaClass(NullType.class);

	  assertTrue(metaChild.isAssignableFrom(metaNull));
  }

  @Test
  public void testNullIsAssignableToChild() throws Exception {
	  // This test checks the valid case:
	  // Child example = null;

	  MetaClass metaChild = getMetaClass(Child.class);
	  MetaClass metaNull = getMetaClass(NullType.class);

	  assertTrue(metaNull.isAssignableTo(metaChild));
  }

  @Test
  public void testIsAssignableFromComparisonForNested() {
    MetaClass interfaceClass = getMetaClass(TestInterface.class);
    MetaClass metaHolderClass = getMetaClass(ObjectWithNested.class);

    // dig out the nested interface from the holder class and ensure it's what we were looking for
    MetaClass nestedInterface = metaHolderClass.getDeclaredClasses()[0];
    assertEquals("MyNestedInterface", nestedInterface.getName());
    assertEquals(getTypeOfMetaClassBeingTested(), nestedInterface.getClass());

    assertTrue("MyNestedInterface should be assignable from TestInterface",
            interfaceClass.isAssignableFrom(nestedInterface));
  }

  @Test
  public void testChildIsAssignableFromChild() {
    // This test checks the valid case:
    // Child example = new Child();

    MetaClass metaChild = getMetaClass(Child.class);

    assertTrue(metaChild.isAssignableFrom(metaChild));
  }

  @Test
  public void testChildIsAssignableToChild() {
    // This test checks the valid case:
    // Child example = new Child();

    MetaClass metaChild = getMetaClass(Child.class);

    assertTrue(metaChild.isAssignableTo(metaChild));
  }

  @Test
  public void testParentIsAssignableFromChild() {
    // This test checks the valid case:
    // Parent example = new Child();

    MetaClass metaChild = getMetaClass(Child.class);
    MetaClass metaParent = getMetaClass(Parent.class);

    assertTrue(metaParent.isAssignableFrom(metaChild));
  }

  @Test
  public void testChildIsNotAssignableFromParent() {
    // This test checks the disallowed case:
    // Child child = new Parent();

    MetaClass metaChild = getMetaClass(Child.class);
    MetaClass metaParent = getMetaClass(Parent.class);

    assertFalse(metaChild.isAssignableFrom(metaParent));
  }

  @Test
  public void testGrandParentIsAssignableFromChild() {
    // This test checks the valid case:
    // Grandparent example = new Child();

    MetaClass metaChild = getMetaClass(Child.class);
    MetaClass metaGrandparent = getMetaClass(Grandparent.class);

    assertTrue(metaGrandparent.isAssignableFrom(metaChild));
  }

  @Test
  public void testChildIsNotAssignableFromGrandParent() {
    // This test checks the disallowed case:
    // Child child = new Grandparent();

    MetaClass metaChild = getMetaClass(Child.class);
    MetaClass metaGrandparent = getMetaClass(Grandparent.class);

    assertFalse(metaChild.isAssignableFrom(metaGrandparent));
  }

  /**
   * This is a regression test for ERRAI-238.
   */
  @Test
  public void testUncleIsAssignableFromChild() {
    // This test checks the valid case:
    // ParentInterface example = new Child();

    MetaClass metaChild = getMetaClass(Child.class);
    MetaClass metaUncle = getMetaClass(ParentInterface.class);

    assertTrue(metaUncle.isAssignableFrom(metaChild));
  }

  @Test
  public void testChildIsNotAssignableFromUncle() {
    // This test checks the disallowed case:
    // Child child = new ParentInterface() {};

    MetaClass metaChild = getMetaClass(Child.class);
    MetaClass metaUncle = getMetaClass(ParentInterface.class);

    assertFalse(metaChild.isAssignableFrom(metaUncle));
  }

  /**
   * This is a regression test for ERRAI-238.
   */
  @Test
  public void testUncleInLawIsAssignableFromChild() {
    // This test checks the valid case:
    // ParentSuperInterface1 example = new Child();

    MetaClass metaChild = getMetaClass(Child.class);
    MetaClass metaUncleInLaw = getMetaClass(ParentSuperInterface1.class);

    assertTrue(metaUncleInLaw.isAssignableFrom(metaChild));
  }

  @Test
  public void testChildIsNotAssignableFromUncleInLaw() {
    // This test checks the disallowed case:
    // Child child = new ParentSuperInterface1() {};

    MetaClass metaChild = getMetaClass(Child.class);
    MetaClass metaUncleInLaw = getMetaClass(ParentSuperInterface1.class);

    assertFalse(metaChild.isAssignableFrom(metaUncleInLaw));
  }

  /**
   * This is a regression test for ERRAI-238.
   */
  @Test
  public void testUncleInLaw2IsAssignableFromChild() {
    // This test checks the valid case:
    // ParentSuperInterface2 example = new Child();

    MetaClass metaChild = getMetaClass(Child.class);
    MetaClass metaUncleInLaw = getMetaClass(ParentSuperInterface2.class);

    assertTrue(metaUncleInLaw.isAssignableFrom(metaChild));
  }

  @Test
  public void testChildIsNotAssignableFromUncleInLaw2() {
    // This test checks the disallowed case:
    // Child child = new ParentSuperInterface2() {};

    MetaClass metaChild = getMetaClass(Child.class);
    MetaClass metaUncleInLaw = getMetaClass(ParentSuperInterface2.class);

    assertFalse(metaChild.isAssignableFrom(metaUncleInLaw));
  }

  /**
   * This is a regression test for ERRAI-238.
   */
  @Test
  public void testGreatUncleIsAssignableFromChild() {
    // This test checks the valid case:
    // GrandparentInterface example = new Child();

    MetaClass metaChild = getMetaClass(Child.class);
    MetaClass metaGreatUncle = getMetaClass(GrandparentInterface.class);

    assertTrue(metaGreatUncle.isAssignableFrom(metaChild));
  }

  @Test
  public void testChildIsNotAssignableFromGreatUncle() {
    // This test checks the disallowed case:
    // Child child = new GrandparentInterface() {};

    MetaClass metaChild = getMetaClass(Child.class);
    MetaClass metaGreatUncle = getMetaClass(GrandparentInterface.class);

    assertFalse(metaChild.isAssignableFrom(metaGreatUncle));
  }

  /**
   * This is a regression test for ERRAI-238.
   */
  @Test
  public void testGreatUncleInLawIsAssignableFromChild() {
    // This test checks the valid case:
    //GrandparentSuperInterface example = new Child();

    MetaClass metaChild = getMetaClass(Child.class);
    MetaClass metaGreatUncleInLaw = getMetaClass(GrandparentSuperInterface.class);

    assertTrue(metaGreatUncleInLaw.isAssignableFrom(metaChild));
  }

  @Test
  public void testChildIsNotAssignableFromGreatUncleInLaw() {
    // This test checks the disallowed case:
    // Child child = new GrandparentSuperInterface() {};

    MetaClass metaChild = getMetaClass(Child.class);
    MetaClass metaGreatUncleInLaw = getMetaClass(GrandparentSuperInterface.class);

    assertFalse(metaChild.isAssignableFrom(metaGreatUncleInLaw));
  }

  @Test
  public void testObjectIsAssignableFromChild() {
    // This test checks the valid case:
    //Object example = new Child();

    MetaClass metaChild = getMetaClass(Child.class);
    MetaClass metaObject = getMetaClass(Object.class);

    assertTrue(metaObject.isAssignableFrom(metaChild));
  }

  @Test
  public void testChildIsNotAssignableFromObject() {
    // This test checks the disallowed case:
    // Child child = new Object();

    MetaClass metaChild = getMetaClass(Child.class);
    MetaClass metaObject = getMetaClass(Object.class);

    assertFalse(metaChild.isAssignableFrom(metaObject));
  }

  @Test
  public void testObjectIsAssignableFromIsolatedInterface() {
    // This test checks the valid case:
    // Object example = new IsolatedInterface() {};

    MetaClass metaInterface = getMetaClass(IsolatedInterface.class);
    MetaClass metaObject = getMetaClass(Object.class);

    assertTrue(metaObject.isAssignableFrom(metaInterface));
  }

  @Test
  public void testIsolatedInterfaceIsNotAssignableFromObject() {
    // This test checks the disallowed case:
    // IsolatedInterface ii = new Object();

    MetaClass metaInterface = getMetaClass(IsolatedInterface.class);
    MetaClass metaObject = getMetaClass(Object.class);

    assertFalse(metaInterface.isAssignableFrom(metaObject));
  }

  @Test
  public void testUncleIsAssignableToChild() {
    // This test checks the allowed case:
	// ParentInterface pi = new Child();

    MetaClass metaChild = getMetaClass(Child.class);
    MetaClass metaUncle = getMetaClass(ParentInterface.class);

    assertTrue(metaChild.isAssignableTo(metaUncle));
  }

}