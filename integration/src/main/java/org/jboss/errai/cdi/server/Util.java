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

import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.naming.*;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.container.ErraiServiceObjectFactory;
import org.jboss.errai.container.ServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Heiko Braun <hbraun@redhat.com>
 * @author Mike Brock <cbrock@redhat.com>
 */
public class Util {
  private static final String COMPONENT_CONTEXT = "java:app";

  private static final String ERRAI_SERVICE_JNDI = COMPONENT_CONTEXT + "/Errai";

  private static final String BEAN_MANAGER_JNDI = "java:comp/BeanManager";
  private static final String BEAN_MANAGER_FALLBACK_JNDI = "java:comp/env/BeanManager";

  private static Logger log = LoggerFactory.getLogger("ErraiJNDI");

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

  private static ErraiServiceObjectFactory factory = new ErraiServiceObjectFactory();

  public static ErraiService lookupErraiService() {
    InitialContext ctx = null;
    ErraiService errai = null;

    boolean bound = false;
    try {
      ctx = new InitialContext();

      NamingEnumeration<NameClassPair> bindings = ctx.list(COMPONENT_CONTEXT);

      while (bindings.hasMoreElements()) {
        NameClassPair n = bindings.next();

        if (n.getName().equals(ERRAI_SERVICE_JNDI) && n.getClassName().equals(ErraiService.class)) {
          bound = true;

          errai = (ErraiService) ctx.lookup(ERRAI_SERVICE_JNDI);
          break;
        }
      }
    }
    catch (NamingException e) {
      log.warn("could not find JNDI binding context: " + COMPONENT_CONTEXT);
    }

    if (!bound && ctx != null) {
      try {
        ctx.rebind(ERRAI_SERVICE_JNDI, new Reference(ErraiService.class.getName(),
                ErraiServiceObjectFactory.class.getName(), null));

        errai = (ErraiService) ctx.lookup(ERRAI_SERVICE_JNDI);
      }
      catch (Exception e) {
        log.warn("JNDI binding failed due to error: " + e.getMessage() + " -- will initialize with singleton.");

        try {
          errai = (ErraiService) factory.getObjectInstance(null, null, null, null);
        }
        catch (Exception e2) {
          throw new RuntimeException("could not initialize ErraiService instance", e);
        }
      }
    }

    return errai;

  }

  public static BeanManager lookupBeanManager() {
    InitialContext ctx = null;
    BeanManager bm = null;

    try {
      ctx = new InitialContext();
      bm = (BeanManager) ctx.lookup(BEAN_MANAGER_JNDI);
    }
    catch (NamingException e) {

      if (ctx != null) {
        try {
          bm = (BeanManager) ctx.lookup(BEAN_MANAGER_FALLBACK_JNDI); // development mode
        }
        catch (NamingException e1) {
        }
      }

      if (null == bm)
        throw new RuntimeException("Failed to locate BeanManager", e);
    }

    return bm;
  }

  public static String resolveServiceName(Class<?> type) {
    String subjectName = type.getAnnotation(Service.class).value();
    if (subjectName.equals(""))
      subjectName = type.getSimpleName();
    return subjectName;
  }
}
