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
