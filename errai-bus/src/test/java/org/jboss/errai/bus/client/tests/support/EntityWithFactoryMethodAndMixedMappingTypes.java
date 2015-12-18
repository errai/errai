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

package org.jboss.errai.bus.client.tests.support;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public final class EntityWithFactoryMethodAndMixedMappingTypes {
  private String fieldInjected;
  private final String factoryMethodInjected;
  private String methodInjected;

  private transient boolean nonMappingConstructorWasCalled;
  private transient boolean factoryMethodWasCalled;
  private transient boolean setterMethodWasCalled;

  /**
   * Factory method used by marshalling framework.
   */
  public static EntityWithFactoryMethodAndMixedMappingTypes instance(@MapsTo("factoryMethodInjected") String factoryMethodInjected) {
    EntityWithFactoryMethodAndMixedMappingTypes instance = new EntityWithFactoryMethodAndMixedMappingTypes(factoryMethodInjected);
    instance.factoryMethodWasCalled = true;
    return instance;
  }

  private EntityWithFactoryMethodAndMixedMappingTypes(String factoryMethodInjected) {
    this.factoryMethodInjected = factoryMethodInjected;
  }

  /**
   * Constructor to be used by test (not by the marshalling framework).
   */
  public EntityWithFactoryMethodAndMixedMappingTypes(String fieldInjected, String factoryMethodInjected, String methodInjected) {
    this.fieldInjected = fieldInjected;
    this.factoryMethodInjected = factoryMethodInjected;
    this.methodInjected = methodInjected;
    nonMappingConstructorWasCalled = true;
  }


  public void setMethodInjected(String methodInjected) {
    this.methodInjected = methodInjected;
    setterMethodWasCalled = true;
  }

  public String getFieldInjected() {
    return fieldInjected;
  }

  public String getFactoryMethodInjected() {
    return factoryMethodInjected;
  }

  public String getMethodInjected() {
    return methodInjected;
  }

  public boolean wasNonMappingConstructorCalled() {
    return nonMappingConstructorWasCalled;
  }

  public boolean wasFactoryMethodCalled() {
    return factoryMethodWasCalled;
  }

  public boolean wasSetterMethodCalled() {
    return setterMethodWasCalled;
  }
}
