/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.bus.server.service.metadata;

import org.jboss.errai.bus.server.ErraiBootstrapFailure;
import org.jboss.errai.bus.server.annotations.ExposeEntity;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;
import org.jboss.errai.bus.server.service.ErraiServiceConfiguratorImpl;
import org.jboss.errai.bus.server.service.bootstrap.BootstrapContext;
import org.jboss.errai.bus.server.service.metadata.MetaDataProcessor;
import org.mvel2.ConversionHandler;
import org.mvel2.DataConversion;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Aug 3, 2010
 */
public class EntityProcessor implements MetaDataProcessor
{
  private Logger log = LoggerFactory.getLogger(EntityProcessor.class);

  public void process(BootstrapContext context, MetaDataScanner reflections)
  {
    final ErraiServiceConfiguratorImpl config = (ErraiServiceConfiguratorImpl)context.getConfig();
    final Set<Class<?>> entities = reflections.getTypesAnnotatedWith(ExposeEntity.class);

    for(Class<?> loadClass : entities)
    {
      log.info("Marked " + loadClass + " as serializable.");
      config.getSerializableTypes().add(loadClass);
      markIfEnumType(loadClass);
    }

    try {
      ResourceBundle bundle = ResourceBundle.getBundle("ErraiApp");
      if (bundle != null) {
        log.info("checking ErraiApp.properties for configured types ...");

        Enumeration<String> keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
          String key = keys.nextElement();
          if (key.equals(ErraiServiceConfigurator.CONFIG_ERRAI_SERIALIZABLE_TYPE)) {
            for (String s : bundle.getString(key).split(" ")) {
              try {
                Class<?> cls = Class.forName(s.trim());
                log.info("Marked " + cls + " as serializable.");                
                config.getSerializableTypes().add(cls);
                markIfEnumType(cls);

              }
              catch (Exception e) {
                throw new ErraiBootstrapFailure(e);
              }
            }

            break;
          }
        }
      }
    }
    catch (MissingResourceException e) {
      log.warn("didn't find any modules to load. (not ErraiApp.properties bundles visible in classpath)");
    }
  }

  private void markIfEnumType(final Class loadClass) {
    if (Enum.class.isAssignableFrom(loadClass)) {
      DataConversion.addConversionHandler(loadClass, new ConversionHandler() {
        public Object convertFrom(Object in) {
          //noinspection unchecked
          return Enum.valueOf((Class<? extends Enum>) loadClass, String.valueOf(in));
        }

        public boolean canConvertFrom(Class cls) {
          return cls == String.class;
        }
      });
    }
  }
}
