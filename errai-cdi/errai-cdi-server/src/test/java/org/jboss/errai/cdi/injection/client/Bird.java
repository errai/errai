package org.jboss.errai.cdi.injection.client;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author Mike Brock
 */
@ApplicationScoped
@SuppressWarnings("unused")
public class Bird {

    private Air air;

    public Bird() {

    }

    @Inject
    public Bird(Air air) {
        this.air = air;
    }

  public Air getAir() {
    return air;
  }
}