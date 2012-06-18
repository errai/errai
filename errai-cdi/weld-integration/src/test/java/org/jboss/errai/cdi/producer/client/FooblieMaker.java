package org.jboss.errai.cdi.producer.client;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Brock
 */
@ApplicationScoped
public class FooblieMaker {
  private final List<Fooblie> destroyedFooblies = new ArrayList<Fooblie>();

  @Produces
  Fooblie produceFooblie() {
    return new Fooblie();
  }

  void destroyFooblie(@Disposes Fooblie fooblie) {
    destroyedFooblies.add(fooblie);
  }

  public List<Fooblie> getDestroyedFooblies() {
    return destroyedFooblies;
  }
}
