package org.jboss.errai.forge.config.converter;

class StringConverter implements ConfigTypeConverter<String> {

  @Override
  public String convertFromString(final String stored) {
    return stored;
  }

  @Override
  public String convertToString(final String toStore) {
    return toStore;
  }

  @Override
  public Class<String> getType() {
    return String.class;
  }

}
