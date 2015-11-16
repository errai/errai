package org.jboss.errai.cdi.injection.client;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author Mike Brock
 */
@ApplicationScoped
public class Air {
  private Bird bird;

  public Air() {
  }

  @Inject
  public Air(Bird bird) {
    this.bird = bird;
  }

  public Bird getBird() {
    return bird;
  }
}