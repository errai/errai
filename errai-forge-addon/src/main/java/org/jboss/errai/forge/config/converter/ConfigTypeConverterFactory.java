package org.jboss.errai.forge.config.converter;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class ConfigTypeConverterFactory {
  
  private static final ConfigTypeConverter<?>[] DEFAULT_CONVERTERS = new ConfigTypeConverter<?>[] {
    new FileConverter(),
    new SetConverter(),
    new StringConverter(),
    new BooleanConverter()
  };
  
  private final Map<Class<?>, ConfigTypeConverter<?>> converterMap;
  
  protected ConfigTypeConverterFactory() {
    this(DEFAULT_CONVERTERS);
  }

  protected ConfigTypeConverterFactory(final ConfigTypeConverter<?>... converters) {
    converterMap = new HashMap<Class<?>, ConfigTypeConverter<?>>();
    
    for (int i = 0; i < converters.length; i++) {
      converterMap.put(converters[i].getType(), converters[i]);
    }
  }
  
  /**
   * @return Never returns null.
   * @throws IllegalArgumentException If no converter exists for the given type.
   */
  public <T> ConfigTypeConverter<T> getConverter(final Class<T> type) throws IllegalArgumentException {
    @SuppressWarnings("unchecked")
    final ConfigTypeConverter<T> converter = (ConfigTypeConverter<T>) converterMap.get(type);
    
    if (converter == null) {
      throw new IllegalArgumentException("There is no converter for the type " + type.getName());
    }
    else {
      return converter;
    }
  }

}
