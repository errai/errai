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

import java.util.Random;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;

import org.jboss.errai.cdi.client.qualifier.A;
import org.jboss.errai.cdi.client.qualifier.B;
import org.jboss.errai.cdi.client.qualifier.C;
import org.jboss.errai.cdi.client.qualifier.D;
import org.jboss.errai.cdi.client.qualifier.E;

/**
 * Test module used by {@see ProducerIntegrationTest}.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Mike Brock
 */
@ApplicationScoped
public class ProducerTestModule {
  @Produces @A
  private Integer numberA = new Random().nextInt();

  private Integer numberB;

  @Produces @B
  public Integer produceNumberB() {
    numberB = new Random().nextInt();
    return numberB;
  }

  private Integer numberC;

  @Produces @C
  public Integer produceNumberC() {
    numberC = 1000;
    return numberC;
  }

  @Produces
  private String produceString(@C Integer number) {
    return Integer.toString(number);
  }

  @Produces @D @E @Default
  private Float floatDE = 1.1f;

  public Integer getNumberA() {
    return numberA;
  }

  public Integer getNumberB() {
    return numberB;
  }

  public Integer getNumberC() {
    return numberC;
  }

  public Float getFloatDE() {
    return floatDE;
  }
}
