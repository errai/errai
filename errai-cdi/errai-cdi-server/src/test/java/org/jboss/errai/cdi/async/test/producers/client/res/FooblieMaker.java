/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.cdi.async.test.producers.client.res;

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
