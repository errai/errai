package org.jboss.errai.cdi.async.test.producers.client.res;

/**
 * @author Mike Brock
 */
public class WrappedKitten {
  private Kitten kitten;

  public WrappedKitten() {
  }

  public WrappedKitten(Kitten kitten) {
    this.kitten = kitten;
  }

  public Kitten getKitten() {
    return kitten;
  }
}
