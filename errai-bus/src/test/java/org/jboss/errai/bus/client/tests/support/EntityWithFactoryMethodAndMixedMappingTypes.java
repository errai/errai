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
