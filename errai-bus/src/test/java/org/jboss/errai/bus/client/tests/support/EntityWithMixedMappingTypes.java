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

/**
 * A portable class that needs a mix of constructor mapping, setter injection, and field injection.
 */
@Portable
public class EntityWithMixedMappingTypes {

  private String fieldInjected;
  private final String constructorInjected;
  private String methodInjected;

  private transient boolean nonMappingConstructorWasCalled;
  private transient boolean mappingConstructorWasCalled;
  private transient boolean setterMethodWasCalled;

  public EntityWithMixedMappingTypes(@MapsTo("constructorInjected") String constructorInjected) {
    this.constructorInjected = constructorInjected;
    mappingConstructorWasCalled = true;
  }

  /**
   * Constructor to be used by test (not by the marshalling framework).
   */
  public EntityWithMixedMappingTypes(String fieldInjected, String constructorInjected, String methodInjected) {
    this.fieldInjected = fieldInjected;
    this.constructorInjected = constructorInjected;
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

  public String getConstructorInjected() {
    return constructorInjected;
  }

  public String getMethodInjected() {
    return methodInjected;
  }

  public boolean wasNonMappingConstructorCalled() {
    return nonMappingConstructorWasCalled;
  }

  public boolean wasMappingConstructorCalled() {
    return mappingConstructorWasCalled;
  }

  public boolean wasSetterMethodCalled() {
    return setterMethodWasCalled;
  }

}
