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

package org.jboss.errai.persistence.server;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.jboss.errai.bus.server.ErraiBootstrapFailure;
import org.jboss.errai.bus.server.annotations.ExtensionComponent;
import org.jboss.errai.bus.server.ext.ErraiConfigExtension;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;
import org.jboss.errai.bus.server.util.ConfigUtil;
import org.jboss.errai.bus.server.util.ConfigVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Entity;
import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;


/**
 * This is a prototype implementation of the persistence service.  It doesn't do anything fancy right now.
 */
@ExtensionComponent
public class ErraiPersistenceConfigurator implements ErraiConfigExtension {
    private ErraiServiceConfigurator config;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    public ErraiPersistenceConfigurator(ErraiServiceConfigurator config) {
        this.config = config;

        logger.info("Configuring persistence extension.");
    }

    public void configure(Map<Class, Provider> bindings, Map<String, Provider> resourceProviders) {
        final AnnotationConfiguration cfg = new AnnotationConfiguration();
        if (!config.hasProperty("errai.prototyping.persistence.connection.driver_class")) {
            return;
        }

        cfg.setProperty("hibernate.connection.driver_class", config.getProperty("errai.prototyping.persistence.connection.driver_class"));
        cfg.setProperty("hibernate.connection.url", config.getProperty("errai.prototyping.persistence.connection.url"));
        cfg.setProperty("hibernate.connection.username", config.getProperty("errai.prototyping.persistence.connection.username"));
        cfg.setProperty("hibernate.connection.password", config.getProperty("errai.prototyping.persistence.connection.password"));
        cfg.setProperty("hibernate.connection.pool_size", config.getProperty("errai.prototyping.persistence.connection.pool_size"));

        cfg.setProperty("hibernate.dialect", config.getProperty("errai.prototyping.persistence.dialect"));
        cfg.setProperty("hibernate.current_session_context_class", "thread");
        cfg.setProperty("hibernate.cache.use_second_level_cache", "false");
        cfg.setProperty("hibernate.cache.provider_class", "org.hibernate.cache.NoCacheProvider");


        cfg.setProperty("hibernate.show_sql", "true");
        cfg.setProperty("hibernate.hbm2ddl.auto", "update");

        List<File> roots = config.getConfigurationRoots();

        logger.info("begin scan for annotated classes.");
        ConfigUtil.visitAllTargets(roots, new ConfigVisitor() {
            public void visit(Class<?> clazz) {
                if (clazz.isAnnotationPresent(Entity.class)) {
                    cfg.addAnnotatedClass(clazz);
                }
            }
        });

        try {
            final SessionFactory sessionFactory = cfg.buildSessionFactory();
            logger.info("finished building hibernate session factory ... ");

            Provider<Session> sessionProvider = new Provider<Session>() {
                public Session get() {
                    return sessionFactory.openSession();
                }
            };
            Provider<SessionFactory> sessionFactoryProvider = new Provider() {
                public Object get() {
                    return sessionFactory;
                }
            };


            logger.info("adding binding for: " + sessionProvider.getClass());
            bindings.put(Session.class, sessionProvider);

            logger.info("adding binding for: " + sessionFactoryProvider.getClass());
            bindings.put(SessionFactory.class, sessionFactoryProvider);


            logger.info("adding resource provider for: " + sessionProvider.getClass());
            resourceProviders.put("SessionProvider", sessionProvider);
        }
        catch (Throwable t) {
            logger.info("session factory did not build: " + t.getClass());            
            t.printStackTrace();
            throw new ErraiBootstrapFailure("could not load errai-persitence", t);
        }
    }


}
