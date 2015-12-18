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

package org.jboss.errai.jpa.client.local;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.ioc.client.api.builtin.IOCProducer;

import com.google.gwt.core.client.GWT;

/**
 * Provides the Errai JPA Entity Manager to client-side code.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@ApplicationScoped
public class ErraiEntityManagerProducer {

  private ErraiEntityManager INSTANCE;

  @IOCProducer
  public ErraiEntityManager getEntityManager() {
    if (INSTANCE == null) {
      ErraiEntityManagerFactory factory = GWT.create(ErraiEntityManagerFactory.class);
      INSTANCE = factory.createEntityManager();
    }
    return INSTANCE;
  }
}
