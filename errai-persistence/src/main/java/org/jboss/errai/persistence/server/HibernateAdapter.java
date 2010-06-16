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

import net.sf.gilead.core.PersistentBeanManager;
import net.sf.gilead.core.hibernate.HibernateUtil;
import net.sf.gilead.core.store.stateful.InMemoryProxyStore;
import org.hibernate.SessionFactory;
import org.jboss.errai.bus.client.framework.ModelAdapter;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Jun 16, 2010
 */
public class HibernateAdapter implements ModelAdapter
{  
  private PersistentBeanManager beanManager;

  public HibernateAdapter(final SessionFactory sessionFactory)
  {
    // configure gilead
    final HibernateUtil persistenceUtil = new HibernateUtil();
    persistenceUtil.setSessionFactory(sessionFactory);

    // TODO: This should actually be a sesion bound proxy store instead of a global one
    final InMemoryProxyStore proxyStore = new InMemoryProxyStore();
    proxyStore.setPersistenceUtil(persistenceUtil);

    beanManager = new PersistentBeanManager();
    beanManager.setPersistenceUtil(persistenceUtil);
    beanManager.setProxyStore(proxyStore);
  }

  public Object clone(Object entity)
  {
    if(entity==null)
      return null;

    return beanManager.clone(entity);
  }

  public Object merge(Object dto)
  {
    if(dto==null)
      return null;
    
    return beanManager.merge(dto);
  }
}
