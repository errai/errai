/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ioc.client.container;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.Alternative;

import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.ioc.client.container.async.AsyncBeanDef;
import org.jboss.errai.ioc.client.container.async.AsyncBeanManager;

/**
 * An adapter that makes the asynchronous bean manager API work with a synchronous bean manager.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Alternative
public class SyncToAsyncBeanManagerAdapter implements AsyncBeanManager {

  private final SyncBeanManager bm;

  public SyncToAsyncBeanManagerAdapter(SyncBeanManager bm) {
    this.bm = bm;
  }

  @Override
  public void destroyBean(Object ref) {
    bm.destroyBean(ref);
  }

  @Override
  public boolean isManaged(Object ref) {
    return bm.isManaged(ref);
  }

  @Override
  public Object getActualBeanReference(Object ref) {
    return bm.getActualBeanReference(ref);
  }

  @Override
  public boolean isProxyReference(Object ref) {
    return bm.isProxyReference(ref);
  }

  @Override
  public boolean addDestructionCallback(Object beanInstance, DestructionCallback<?> destructionCallback) {
    return bm.addDestructionCallback(beanInstance, destructionCallback);
  }

  @Override
  public void destroyAllBeans() {
    bm.destroyAllBeans();
  }

  @Override
  @SuppressWarnings("rawtypes")
  public Collection<AsyncBeanDef> lookupBeans(String name) {
    final Collection<SyncBeanDef> beanDefs = bm.lookupBeans(name);

    final List<AsyncBeanDef> asyncBeanDefs = new ArrayList<AsyncBeanDef>();
    for (final SyncBeanDef beanDef : beanDefs) {
      asyncBeanDefs.add(createAsyncBeanDef(beanDef));
    }

    return asyncBeanDefs;
  }

  @Override
  public <T> Collection<AsyncBeanDef<T>> lookupBeans(Class<T> type) {
    return lookupBeans(type, new Annotation[0]);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> Collection<AsyncBeanDef<T>> lookupBeans(Class<T> type, Annotation... qualifiers) {
    final Collection<SyncBeanDef<T>> beanDefs = bm.lookupBeans(type, qualifiers);

    final List<AsyncBeanDef<T>> asyncBeanDefs = new ArrayList<AsyncBeanDef<T>>();
    for (final SyncBeanDef<T> beanDef : beanDefs) {
      asyncBeanDefs.add(createAsyncBeanDef(beanDef));
    }

    return asyncBeanDefs;
  }

  @Override
  @SuppressWarnings({ "unchecked" })
  public <T> AsyncBeanDef<T> lookupBean(Class<T> type, Annotation... qualifiers) {
    final SyncBeanDef<T> beanDef = bm.lookupBean(type, qualifiers);
    return createAsyncBeanDef(beanDef);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private AsyncBeanDef createAsyncBeanDef(final SyncBeanDef beanDef) {
    AsyncBeanDef abd = new AsyncBeanDef() {

      @Override
      public Class getType() {
        return beanDef.getType();
      }

      @Override
      public Class<?> getBeanClass() {
        return beanDef.getBeanClass();
      }

      @Override
      public Class<? extends Annotation> getScope() {
        return beanDef.getScope();
      }

      @Override
      public void getInstance(CreationalCallback callback) {
        callback.callback(beanDef.getInstance());
      }

      @Override
      public void newInstance(CreationalCallback callback) {
        callback.callback(beanDef.newInstance());
      }

      @Override
      public Set getQualifiers() {
        return beanDef.getQualifiers();
      }

      @Override
      public boolean matches(Set annotations) {
        return beanDef.matches(annotations);
      }

      @Override
      public String getName() {
        return beanDef.getName();
      }

      @Override
      public boolean isActivated() {
        return beanDef.isActivated();
      }

      @Override
      public boolean isAssignableTo(Class type) {
        return beanDef.isAssignableTo(type);
      }
    };

    return abd;
  }
}
