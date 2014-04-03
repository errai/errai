package org.jboss.errai.forge.config.converter;

import java.io.File;

public class FileConverter implements ConfigTypeConverter<File> {

  @Override
  public File convertFromString(final String stored) {
    return new File(stored);
  }

  @Override
  public String convertToString(final File toStore) {
    return toStore.getAbsolutePath();
  }

  @Override
  public Class<File> getType() {
    return File.class;
  }

}
