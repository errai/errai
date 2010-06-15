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
package org.jboss.errai.test.persistence;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Jun 11, 2010
 */
public class Configuration
{
  private SessionFactory sessionFactory;

  public Configuration()
  {
    final AnnotationConfiguration cfg = new AnnotationConfiguration();

    String dbLocation = System.getProperty("user.home") + "/ErraiPersistenceDB";

    System.out.println("DB Location: "+ dbLocation);

    cfg.setProperty("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver");
    cfg.setProperty("hibernate.connection.url", "jdbc:hsqldb:file:" + dbLocation);
    cfg.setProperty("hibernate.connection.username", "sa");
    cfg.setProperty("hibernate.connection.password", "");
    cfg.setProperty("hibernate.connection.pool_size", "1");

    cfg.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
    cfg.setProperty("hibernate.current_session_context_class", "thread");
    cfg.setProperty("hibernate.cache.use_second_level_cache", "false");
    cfg.setProperty("hibernate.cache.provider_class", "org.hibernate.cache.NoCacheProvider");

    cfg.setProperty("hibernate.show_sql", "true");
    cfg.setProperty("hibernate.hbm2ddl.auto", "create-drop");

    cfg.configure("hibernate.cfg.xml");

    this.sessionFactory = cfg.buildSessionFactory();
  }

  public SessionFactory getSessionFactory() {
    return sessionFactory;
  }
}