package org.jboss.errai.bus.client.tests.support;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.marshalling.client.api.annotations.MapsTo;

/**
 * @author Mike Brock
 */
@Portable
public class ImplicitEnumContainer {
  private final ImplicitEnum implicitEnum;

  public ImplicitEnumContainer(@MapsTo("implicitEnum") ImplicitEnum implicitEnum) {
    this.implicitEnum = implicitEnum;
  }

  public ImplicitEnum getImplicitEnum() {
    return implicitEnum;
  }
}
