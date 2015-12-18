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
  private final List<Fooblie> destroyedFoobliesResponse = new ArrayList<Fooblie>();
  private final List<Fooblie> destroyedFoobliesGreets = new ArrayList<Fooblie>();
  private final List<Fooblie> destroyedFoobliesParts = new ArrayList<Fooblie>();

  @Produces
  @Response
  Fooblie produceFooblie() {
    return new Fooblie(Response.class.getName());
  }

  @Produces
  @Greets
  Fooblie produceFooblieGreets() {
    return new Fooblie(Greets.class.getName());
  }

  @Produces
  @Parts
  Fooblie produceFooblieParts() {
    return new Fooblie(Parts.class.getName());
  }

  void destroyFooblie(@Disposes @Response Fooblie fooblie) {
    destroyedFoobliesResponse.add(fooblie);
  }

  void destroyFooblieGreets(@Disposes @Greets Fooblie fooblie) {
    destroyedFoobliesGreets.add(fooblie);
  }

  void destroyFooblieParts(@Disposes @Parts Fooblie fooblie) {
    destroyedFoobliesParts.add(fooblie);
  }

  public List<Fooblie> getDestroyedFoobliesResponse() {
    return destroyedFoobliesResponse;
  }

  public List<Fooblie> getDestroyedFoobliesGreets() {
    return destroyedFoobliesGreets;
  }

  public List<Fooblie> getDestroyedFoobliesParts() {
    return destroyedFoobliesParts;
  }
}
