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

import java.lang.annotation.Annotation;
import java.util.Arrays;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.server.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Heiko Braun <hbraun@redhat.com>
 * @author Mike Brock <cbrock@redhat.com>
 */
public class CDIServerUtil {
  private static final String COMPONENT_CONTEXT = "java:comp/env";

  private static final String BEAN_MANAGER_JNDI = "java:comp/BeanManager";
  private static final String BEAN_MANAGER_FALLBACK_JNDI = "java:comp/env/BeanManager";

  private static Logger log = LoggerFactory.getLogger("ErraiJNDI");

  @SuppressWarnings("unchecked")
  public static <T> T lookupBean(BeanManager beanManager, Class<T> serviceType) {
    Bean<?> bean = beanManager.resolve(beanManager.getBeans(serviceType));

    if (bean == null) {
      return null;
    }

    return (T) beanManager.getReference(bean, serviceType, beanManager.createCreationalContext(bean));
  }

  public static QueueSession getSession(Message message) {
    return message.getResource(QueueSession.class, "Session");
  }


  @SuppressWarnings("unchecked")
  public static <T> T lookupRPCBean(BeanManager beanManager, Class beanClass, Annotation[] annotations) {
    Bean<?> bean;

    if (annotations != null) {
      bean = beanManager.resolve(beanManager.getBeans(beanClass, annotations));
    }
    else {
      bean = beanManager.resolve(beanManager.getBeans(beanClass));
    }

    CreationalContext<?> context = beanManager.createCreationalContext(bean);
    return (T) beanManager.getReference(bean, beanClass, context);

  }

  /**
   * Attempts to get a reference to the CDI BeanManager by searching for it in
   * in the JNDI InitialContext. Several locations are searched.
   *
   * @return The BeanManager that was retrieved from JNDI. Never null.
   * @throws NamingException
   *           If the BeanManager could not be found at any of the possible JNDI
   *           paths.
   */
  public static BeanManager lookupBeanManager() throws NamingException {
    final String[] names = { BEAN_MANAGER_JNDI, BEAN_MANAGER_FALLBACK_JNDI };

    BeanManager bm = null;

    InitialContext ctx = new InitialContext();
    for (String name : names) {
      try {
        bm = (BeanManager) ctx.lookup(name);
      }
      catch (NamingException e) {
        // allow loop to try again
      }
    }

    if (bm == null) {
      throw new NamingException("Failed to locate the CDI BeanManager under any of the JNDI names: " + Arrays.toString(names));
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
