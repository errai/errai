/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.cdi.producer.client;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 * @author Mike Brock
 */
@ApplicationScoped
public class BeanConstrConsumesOwnProducer {
  WrappedThing thing;
  ProducerFactory factory;

  // needed because of cycle in main constructor
  public BeanConstrConsumesOwnProducer() {
  }

  @Inject
  public BeanConstrConsumesOwnProducer(@Produced WrappedThing thing, ProducerFactory factory) {
    this.thing = thing;
    this.factory = factory;
  }

  @Produces @Produced @ApplicationScoped
  private WrappedThing produceWrappedThing(@Produced Thing thing) {
    return new WrappedThing(thing);
  }

  public WrappedThing getThing() {
    return thing;
  }

  public ProducerFactory getFactory() {
    return factory;
  }
}
