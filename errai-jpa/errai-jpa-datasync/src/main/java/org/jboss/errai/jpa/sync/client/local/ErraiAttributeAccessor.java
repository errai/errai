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

package org.jboss.errai.jpa.sync.client.local;

import javax.persistence.metamodel.Attribute;

import org.jboss.errai.jpa.client.local.ErraiAttribute;
import org.jboss.errai.jpa.sync.client.shared.JpaAttributeAccessor;

/**
 * Implementation of {@link JpaAttributeAccessor} that works with Errai's
 * generated client-side Attribute objects (which have their own get() and set()
 * methods).
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
public class ErraiAttributeAccessor implements JpaAttributeAccessor {

  @Override
  public <X, Y> Y get(Attribute<X, Y> attribute, X entity) {
    return ((ErraiAttribute<X, Y>) attribute).get(entity);
  }

  @Override
  public <X, Y> void set(Attribute<X, Y> attribute, X entity, Y value) {
    ((ErraiAttribute<X, Y>) attribute).set(entity, value);
  }

}
