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

package org.jboss.errai.forge.config.converter;

import org.jboss.errai.forge.config.SerializableSet;

class SetConverter implements ConfigTypeConverter<SerializableSet> {

  @Override
  public SerializableSet convertFromString(final String stored) {
    return SerializableSet.deserialize(stored);
  }

  @Override
  public String convertToString(final SerializableSet toStore) {
    return toStore.serialize();
  }

  @Override
  public Class<SerializableSet> getType() {
    return SerializableSet.class;
  }

}
