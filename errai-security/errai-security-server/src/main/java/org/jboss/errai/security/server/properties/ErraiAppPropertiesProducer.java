/**
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.security.server.properties;

import static org.jboss.errai.security.Properties.USER_ON_HOSTPAGE_ENABLED;
import static org.jboss.errai.security.Properties.USER_COOKIE_ENABLED;

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
    
    final InputStream erraiAppPropertiesStream = 
            getClass().getClassLoader().getResourceAsStream("ErraiApp.properties");
    try {
      if (erraiAppPropertiesStream != null) {
        properties.load(erraiAppPropertiesStream);
        erraiAppPropertiesStream.close();
        
        if (!properties.containsKey(USER_ON_HOSTPAGE_ENABLED) && 
                !properties.containsKey(USER_COOKIE_ENABLED)) {
          // This is due to the backport of the user_on_hostpage fix in 3.2:
          // - The user_cookie_enabled property did not exist in 3.0 
          // - We want to avoid having to add this property to every single app which is why we 
          // programmatically default to true
          // More details:  https://bugzilla.redhat.com/show_bug.cgi?id=1222570
          properties.setProperty(USER_COOKIE_ENABLED, "true");
        }
      }
    }
    catch (IOException e) {
      logger.warn("An error occurred reading the ErraiApp.properties stream.", e);
    }

    return properties;
  }

}
