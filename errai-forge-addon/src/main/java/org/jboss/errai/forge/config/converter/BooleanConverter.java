package org.jboss.errai.forge.config.converter;

public class BooleanConverter implements ConfigTypeConverter<Boolean> {

  @Override
  public Boolean convertFromString(final String stored) {
    return Boolean.valueOf(stored);
  }

  @Override
  public String convertToString(final Boolean toStore) {
    return toStore.toString();
  }

  @Override
  public Class<Boolean> getType() {
    return Boolean.class;
  }

}
