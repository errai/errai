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

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;

import net.sf.gilead.core.IPersistenceUtil;
import net.sf.gilead.core.PersistentBeanManager;
import net.sf.gilead.core.hibernate.HibernateUtil;
import net.sf.gilead.core.hibernate.jboss.HibernateJBossUtil;
import net.sf.gilead.core.hibernate.jpa.HibernateJpaUtil;
import net.sf.gilead.core.store.stateful.InMemoryProxyStore;
import org.hibernate.SessionFactory;
import org.jboss.errai.bus.client.framework.ModelAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @author Marcin Misiewicz
 * 
 * @date: Jun 16, 2010
 */
public class HibernateAdapter implements ModelAdapter {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private PersistentBeanManager beanManager;
	private String jndiName;
	private boolean useJbossUtil;
	private boolean usingJpa;

	/**
	 * Create adapter.
	 * 
	 * @param jndiName - factory jndi name
	 * @param useJbossUtil - use jboss specific gilead persistence util
	 * @param usingJpa - if true {@link PersistentBeanManager} will be configured with {@link EntityManagerFactory} 
	 * 			otherwise with {@link SessionFactory}
	 */
    public HibernateAdapter(String jndiName, boolean useJbossUtil, boolean usingJpa) {
		this.jndiName = jndiName;
		this.useJbossUtil = useJbossUtil;
		this.usingJpa = usingJpa;		
    }

    /**
     *  Lazy initialization of the {@link PersistentBeanManager}. 
     *  At this time factory should be bound in the jndi.
     */
	protected void initPersistentBeanManager() {
		logger.debug("Initializing PersistentBeanManager");
		Object factory;
		try {
			factory = new InitialContext().lookup(jndiName);
		} catch (NamingException e) {
			logger.error("Cold not lookup : "+jndiName);
			e.printStackTrace();
			return;
		}
		IPersistenceUtil util;
		if (useJbossUtil) {
			if (usingJpa) {
				util = new HibernateJBossUtil((EntityManagerFactory)factory);
			} else {
				util = new HibernateJBossUtil((SessionFactory) factory);
			}
		} else {
			if (usingJpa) {
				util = new HibernateJpaUtil((EntityManagerFactory)factory);
			} else {
				util = new HibernateUtil((SessionFactory) factory);
			}			
		}
		
        // TODO: This should actually be a session bound proxy store instead of a global one
		InMemoryProxyStore proxyStore = new InMemoryProxyStore();
		proxyStore.setPersistenceUtil(util);
		beanManager = PersistentBeanManager.getInstance();
		beanManager.setPersistenceUtil(util);
		beanManager.setProxyStore(proxyStore);
	}
    
    public Object clone(Object entity) {
        if (entity == null)
            return null;
        
		if (beanManager == null) {
			initPersistentBeanManager();
		}

        return beanManager.clone(entity);
    }

    public Object merge(Object dto) {
        if (dto == null)
            return null;
        
		if (beanManager == null) {
			initPersistentBeanManager();
		}

        return beanManager.merge(dto);
    }
}
