package org.jboss.errai.cdi.producer.client;

/**
 * @author Mike Brock
 */
public class WrappedThing {
  private Thing thing;

  public WrappedThing() {
  }

  public WrappedThing(Thing thing) {
    this.thing = thing;
  }

  public Thing getThing() {
    return thing;
  }

  public void setThing(Thing thing) {
    this.thing = thing;
  }
}
