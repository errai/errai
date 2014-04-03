package org.jboss.errai.forge.config.converter;

public interface ConfigTypeConverter<T> {
  
  T convertFromString(String stored);
  
  String convertToString(T toStore);
  
  Class<T> getType();

}
