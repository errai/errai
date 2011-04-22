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
package org.jboss.errai.cdi.server;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.container.ServiceFactory;
import org.jboss.weld.environment.se.Weld;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Set;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Sep 15, 2010
 */
public class Util {
    private static final String ERRAI_SERVICE_JNDI = "java:comp/ErraiService";

    private static final String BEAN_MANAGER_JNDI = "java:comp/BeanManager";
    private static final String BEAN_MANAGER_FALLBACK_JNDI = "java:comp/env/BeanManager";

    public static Object lookupCallbackBean(BeanManager beanManager, Class<?> serviceType) {
        Set<Bean<?>> beans = beanManager.getBeans(serviceType);
        Bean<?> bean = beanManager.resolve(beans);

        if (bean == null) {
            return null;
        }

        CreationalContext<?> context = beanManager.createCreationalContext(bean);
        return beanManager.getReference(bean, serviceType, context);
    }

    public static String getSessionId(Message message) {
        String sessionID = message.getResource(String.class, "SessionID");
        return sessionID;
    }

    public static <T> T lookupRPCBean(BeanManager beanManager, T rpcIntf, Class beanClass) {
        Set<Bean<?>> beans = beanManager.getBeans(beanClass);
        Bean<?> bean = beanManager.resolve(beans);
        CreationalContext<?> context = beanManager.createCreationalContext(bean);
        return (T) beanManager.getReference(bean, beanClass, context);

    }

    @Deprecated
    public static MessageBus lookupMessageBus() {
        InitialContext ctx = null;
        ErraiService errai = null;

        try {
            ctx = new InitialContext();
            errai = (ErraiService) ctx.lookup("java:/Errai");
        } catch (NamingException e) {
            if (ctx != null) {
                try {
                    errai = (ErraiService) ctx.lookup("java:comp/env/Errai"); // development mode
                } catch (NamingException e1) {
                }
            }

            if (null == errai)
                throw new RuntimeException("Failed to locate Errai service instance", e);
        }

        return errai.getBus();
    }

    public static ErraiService lookupErraiService() {

        InitialContext ctx = null;
        ErraiService erraiService = null;

        try {
            ctx = new InitialContext();
            erraiService = (ErraiService) ctx.lookup(ERRAI_SERVICE_JNDI);
        } catch (NamingException e) {

            if (ctx != null) {
                try {
                    erraiService = ServiceFactory.create();
                    ctx.bind(ERRAI_SERVICE_JNDI, erraiService);

                } catch (NamingException e1) {
                }
            }

            if (null == erraiService)
                throw new RuntimeException("Failed to locate or create ErraiService", e);
        }

        return erraiService;
    }

    public static BeanManager lookupBeanManager() {
        InitialContext ctx = null;
        BeanManager bm = null;

        try {
            ctx = new InitialContext();
            bm = (BeanManager) ctx.lookup(BEAN_MANAGER_JNDI);
        } catch (NamingException e) {

            if (ctx != null) {
                try {
                    bm = (BeanManager) ctx.lookup(BEAN_MANAGER_FALLBACK_JNDI); // development mode
                } catch (NamingException e1) {
                }
            }

            if (null == bm)
                throw new RuntimeException("Failed to locate BeanManager", e);
        }

        return bm;
    }

    public static String resolveServiceName(Class<?> type) {
        String subjectName = type.getAnnotation(Service.class).value();
        if (subjectName.equals("")) subjectName = type.getSimpleName();
        return subjectName;
    }
}
