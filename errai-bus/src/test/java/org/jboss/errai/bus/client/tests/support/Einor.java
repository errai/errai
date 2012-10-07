package org.jboss.errai.bus.client.tests.support;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * @author Mike Brock
 */
@Portable
public final class Einor {
  private Genie<Einor> genie;

  public Genie<Einor> getGenie() { return genie; }
  public void setGenie(Genie<Einor> genie) { this.genie = genie; }
}