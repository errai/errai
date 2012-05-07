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

  public static String resolveServiceName(Class<?> type) {
    String subjectName = type.getAnnotation(Service.class).value();

    if (subjectName.equals(""))
      subjectName = type.getSimpleName();

    return subjectName;
  }
}
