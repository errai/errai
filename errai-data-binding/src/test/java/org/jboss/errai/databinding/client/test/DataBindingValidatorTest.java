/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.databinding.client.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.config.ErraiAppPropertiesConfiguration;
import org.jboss.errai.databinding.client.TestModel;
import org.jboss.errai.databinding.rebind.DataBindingValidator;
import org.junit.Test;

import java.util.Set;

/**
 * Tests for the {@link DataBindingValidator} rebind utility.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
public class DataBindingValidatorTest {

  private static final Set<MetaClass> ALL_CONFIGURED_BINDABLE_TYPES = new ErraiAppPropertiesConfiguration().modules().getBindableTypes();

  private final MetaClass testClass = MetaClassFactory.get(TestModel.class);

  @Test
  public void testLeadingDotFails() {
    assertFalse(isValidPropertyChain(testClass, ".name"));
  }
  
  @Test
  public void testTrailingDotFails() {
    assertFalse(isValidPropertyChain(testClass, "name."));
  }
  
  @Test
  public void testNonBindableTypeFails() {
    assertFalse(isValidPropertyChain(MetaClassFactory.get(String.class), "length"));
  }
  
  @Test
  public void testSinglePropertyPasses() {
    assertTrue(isValidPropertyChain(testClass, "value"));
  }
  
  @Test
  public void testPropertyChainPasses() {
    assertTrue(isValidPropertyChain(testClass, "child.child.value"));
  }
  
  @Test
  public void testPropertyChainFails() {
    assertFalse(isValidPropertyChain(testClass, "child.value.value"));
  }
  
  @Test
  public void testDoubleDotInChainFails() {
    assertFalse(isValidPropertyChain(testClass, "child.child..value"));
  }
  
  @Test
  public void testLeadingAndTrailingDotInChainFails() {
    assertFalse(isValidPropertyChain(testClass, ".child.child.value."));
  }
  
  @Test(expected=NullPointerException.class)
  public void testExceptionOnNullType() {
    isValidPropertyChain(null, "child");
  }

  @Test(expected=NullPointerException.class)
  public void testExceptionOnNullPropertyChain() {
    isValidPropertyChain(testClass, null);
  }

  private static boolean isValidPropertyChain(final MetaClass bindableType, final String propertyChain) {
    return DataBindingValidator.isValidPropertyChain(bindableType, propertyChain, ALL_CONFIGURED_BINDABLE_TYPES);
  }
}
