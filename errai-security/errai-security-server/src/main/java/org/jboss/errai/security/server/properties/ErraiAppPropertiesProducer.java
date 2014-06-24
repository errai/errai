package org.jboss.errai.security.server.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.enterprise.inject.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates {@link Properties} instance from the ErraiApp.properties resource.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class ErraiAppPropertiesProducer {

  private static final Logger logger = LoggerFactory.getLogger(ErraiAppPropertiesProducer.class);

  @Produces
  @ErraiAppProperties
  public Properties getErraiAppProperties() {
    final Properties properties = new Properties();
    final InputStream erraiAppPropertiesStream = ClassLoader.getSystemResourceAsStream("ErraiApp.properties");
    try {
      if (erraiAppPropertiesStream != null) {
        properties.load(erraiAppPropertiesStream);
        erraiAppPropertiesStream.close();
      }
    }
    catch (IOException e) {
      logger.warn("An error occurred reading the ErraiApp.properties stream.", e);
    }

    return properties;
  }

}
