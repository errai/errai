package org.jboss.errai.bus.client.tests.support;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * @author Mike Brock
 */
@Portable(aliasOf = Moron.class)
public class SubMoron extends Moron {
  private String dumbFieldThatShouldntBeMarshalled;

  public SubMoron() {
  }

  public SubMoron(String value) {
    super(value);
  }

  public String getDumbFieldThatShouldntBeMarshalled() {
    return dumbFieldThatShouldntBeMarshalled;
  }

  public void setDumbFieldThatShouldntBeMarshalled(String dumbFieldThatShouldntBeMarshalled) {
    this.dumbFieldThatShouldntBeMarshalled = dumbFieldThatShouldntBeMarshalled;
  }
}
