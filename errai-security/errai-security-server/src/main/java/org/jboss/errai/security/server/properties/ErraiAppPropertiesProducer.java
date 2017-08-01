/*
 * Copyright (C) 2014 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.security.server.properties;

import org.jboss.errai.common.metadata.ErraiAppPropertiesFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Produces;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * Creates {@link Properties} instance from the ErraiApp.properties and/or META-INF ErraiApp.properties resource.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class ErraiAppPropertiesProducer {

  private static final Logger logger = LoggerFactory.getLogger(ErraiAppPropertiesProducer.class);

  @Produces
  @ErraiAppProperties
  public Properties getErraiAppProperties() {
    final Properties properties = new Properties();

    ErraiAppPropertiesFiles.getUrls(getClass().getClassLoader())
            .stream()
            .map(this::loadUrlStreamToProperties)
            .forEach(properties::putAll);

    return properties;
  }

  private Properties loadUrlStreamToProperties(final URL url) {
    final Properties properties = new Properties();

    try {
      properties.load(url.openStream());
    } catch (IOException e) {
      logger.warn("An error occurred reading the ErraiApp.properties stream.", e);
    }

    return properties;
  }

}
