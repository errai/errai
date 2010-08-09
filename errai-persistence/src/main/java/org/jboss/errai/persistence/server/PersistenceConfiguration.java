/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.persistence.server;

import com.google.inject.Inject;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.jboss.errai.bus.client.api.ResourceProvider;
import org.jboss.errai.bus.client.framework.ModelAdapter;
import org.jboss.errai.bus.server.ErraiBootstrapFailure;
import org.jboss.errai.bus.server.annotations.ExtensionComponent;
import org.jboss.errai.bus.server.api.ErraiConfig;
import org.jboss.errai.bus.server.api.ErraiConfigExtension;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;
import org.jboss.errai.bus.server.service.metadata.MetaDataScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Entity;
import java.util.Set;


/**
 * Configures the hibernate SessionFactory and make it available
 * as an injection point in guice ( see {@link ResourceProvider} ).
 *
 *
 */
@ExtensionComponent
public class PersistenceConfiguration implements ErraiConfigExtension {
  private ErraiServiceConfigurator configurator;
  private Logger logger = LoggerFactory.getLogger(this.getClass());

  @Inject
  public PersistenceConfiguration(ErraiServiceConfigurator configurator) {
    this.configurator = configurator;

    logger.info("Configuring persistence extension.");
  }

  public void configure(ErraiConfig config) {
    final AnnotationConfiguration cfg = new AnnotationConfiguration();
    if (!configurator.hasProperty("errai.prototyping.persistence.connection.driver_class")) {
      return;
    }

    cfg.setProperty("hibernate.connection.driver_class", configurator.getProperty("errai.prototyping.persistence.connection.driver_class"));
    cfg.setProperty("hibernate.connection.url", configurator.getProperty("errai.prototyping.persistence.connection.url"));
    cfg.setProperty("hibernate.connection.username", configurator.getProperty("errai.prototyping.persistence.connection.username"));
    cfg.setProperty("hibernate.connection.password", configurator.getProperty("errai.prototyping.persistence.connection.password"));
    cfg.setProperty("hibernate.connection.pool_size", configurator.getProperty("errai.prototyping.persistence.connection.pool_size"));

    cfg.setProperty("hibernate.dialect", configurator.getProperty("errai.prototyping.persistence.dialect"));
    cfg.setProperty("hibernate.current_session_context_class", "thread");
    cfg.setProperty("hibernate.cache.use_second_level_cache", "false");
    cfg.setProperty("hibernate.cache.provider_class", "org.hibernate.cache.NoCacheProvider");

    cfg.setProperty("hibernate.show_sql", "true");
    cfg.setProperty("hibernate.hbm2ddl.auto", "update");

    logger.debug("begin scan for annotated classes.");
    MetaDataScanner scanner = configurator.getMetaDataScanner();
    Set<Class<?>> entities = scanner.getTypesAnnotatedWith(Entity.class);
    for(Class<?> entity : entities)
    {
      cfg.addAnnotatedClass(entity);
    }

    try {
      final SessionFactory sessionFactory = cfg.buildSessionFactory();
      logger.info("finished building hibernate session factory ... ");

      ResourceProvider<Session> sessionProvider = new ResourceProvider<Session>() {
        public Session get() {
          return sessionFactory.openSession();
        }
      };
      ResourceProvider<SessionFactory> sessionFactoryProvider = new ResourceProvider() {
        public Object get() {
          return sessionFactory;
        }
      };

      final ModelAdapter hibenateAdapter = new HibernateAdapter(sessionFactory);
      ResourceProvider<ModelAdapter> modelAdapterProvider = new ResourceProvider<ModelAdapter>()
      {
        public ModelAdapter get()
        {
          return hibenateAdapter;
        }
      };
      logger.info("adding binding for: " + hibenateAdapter.getClass());
      config.addBinding(ModelAdapter.class, modelAdapterProvider);

      logger.info("adding binding for: " + sessionProvider.getClass());
      config.addBinding(Session.class, sessionProvider);

      logger.info("adding binding for: " + sessionFactoryProvider.getClass());
      config.addBinding(SessionFactory.class, sessionFactoryProvider);

      logger.info("adding resource provider for: " + sessionProvider.getClass());
      config.addResourceProvider("SessionProvider", sessionProvider);
    }
    catch (Throwable t) {
      logger.info("session factory did not build: " + t.getClass());
      t.printStackTrace();
      throw new ErraiBootstrapFailure("could not load errai-persitence", t);
    }
  }


}
