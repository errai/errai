/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.jboss.errai.ioc.client;

import org.jboss.errai.ioc.client.container.BeanProvider;
import org.jboss.errai.ioc.client.container.CreationalContext;
import org.jboss.errai.ioc.client.container.IOCBeanDef;
import org.jboss.errai.ioc.client.container.IOCResolutionException;
import org.jboss.errai.ioc.client.container.SimpleCreationalContext;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanManager;

import javax.enterprise.context.ApplicationScoped;
import java.lang.annotation.Annotation;
import java.util.Iterator;

public class SimpleInjectionContext implements BootstrapInjectionContext {
  public static final Object LAZY_INIT_REF = new Object();

  private final IOCBeanManager manager;
  private final SimpleCreationalContext rootContext;

  public SimpleInjectionContext() {
    manager = IOC.getBeanManager();
    rootContext = new SimpleCreationalContext(true, manager, ApplicationScoped.class);
  }

  @SuppressWarnings("unchecked")
  public void addBean(final Class type,
                      final Class beanType,
                      final BeanProvider callback,
                      Object instance,
                      final Annotation[] qualifiers) {

    if (instance == SimpleInjectionContext.LAZY_INIT_REF) {
      try {
        manager.lookupBean(type, qualifiers);
        return;
      }
      catch (IOCResolutionException e) {
        instance = callback.getInstance(rootContext);
      }
    }

    manager.addBean(type, beanType, callback, instance, qualifiers);
  }

  @SuppressWarnings("unchecked")
  public void addBean(final Class type,
                      final Class beanType,
                      final BeanProvider callback,
                      Object instance,
                      final Annotation[] qualifiers,
                      final String name) {

    if (instance == SimpleInjectionContext.LAZY_INIT_REF) {
      try {
        manager.lookupBean(type, qualifiers);
        return;
      }
      catch (IOCResolutionException e) {
        instance = callback.getInstance(rootContext);
      }
    }

    manager.addBean(type, beanType, callback, instance, qualifiers, name);
  }

  @SuppressWarnings("unchecked")
  public void addBean(final Class type,
                      final Class beanType,
                      final BeanProvider callback,
                      Object instance,
                      final Annotation[] qualifiers,
                      final String name,
                      final boolean concrete) {

    if (instance == SimpleInjectionContext.LAZY_INIT_REF) {
      try {
        manager.lookupBean(type, qualifiers);
        return;
      }
      catch (IOCResolutionException e) {
        instance = callback.getInstance(rootContext);
      }
    }

    manager.addBean(type, beanType, callback, instance, qualifiers, name, concrete);
  }

  public SimpleCreationalContext getRootContext() {
    return rootContext;
  }
}
