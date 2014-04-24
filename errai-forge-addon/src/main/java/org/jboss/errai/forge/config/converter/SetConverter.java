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
