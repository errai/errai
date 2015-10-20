/*
 * Copyright 2015 JBoss, by Red Hat, Inc
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

package org.jboss.errai.ioc.client.container.async;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;

import javax.enterprise.inject.Alternative;

import org.jboss.errai.ioc.client.container.BeanManagerSetup;
import org.jboss.errai.ioc.client.container.ContextManager;
import org.jboss.errai.ioc.client.container.DestructionCallback;
import org.jboss.errai.ioc.client.container.IOCBeanDef;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.jboss.errai.ioc.client.container.SyncBeanManagerImpl;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Alternative
public class AsyncBeanManagerImpl implements AsyncBeanManager, BeanManagerSetup {

  private final SyncBeanManagerImpl innerBeanManager = new SyncBeanManagerImpl();

  @Override
  public void destroyBean(final Object ref) {
    innerBeanManager.destroyBean(ref);
  }

  @Override
  public boolean isManaged(final Object ref) {
    return innerBeanManager.isManaged(ref);
  }

  @Override
  public Object getActualBeanReference(final Object ref) {
    return innerBeanManager.getActualBeanReference(ref);
  }

  @Override
  public boolean isProxyReference(final Object ref) {
    return innerBeanManager.isProxyReference(ref);
  }

  @Override
  public boolean addDestructionCallback(final Object beanInstance, final DestructionCallback<?> destructionCallback) {
    return innerBeanManager.addDestructionCallback(beanInstance, destructionCallback);
  }

  @Override
  public void destroyAllBeans() {
    innerBeanManager.destroyAllBeans();
  }

  @Override
  public void destroyBean(Object ref, Runnable runnable) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not yet implemented.");
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public Collection<AsyncBeanDef> lookupBeans(String name) {
    final Collection syncBeans = innerBeanManager.lookupBeans(name);
    final Collection wrapSyncBeans = wrapSyncBeans(syncBeans);

    return wrapSyncBeans;
  }

  private <T> Collection<AsyncBeanDef<T>> wrapSyncBeans(final Collection<IOCBeanDef<T>> syncBeans) {
    final Collection<AsyncBeanDef<T>> asyncBeans = new ArrayList<AsyncBeanDef<T>>(syncBeans.size());

    for (final IOCBeanDef<T> syncBean : syncBeans) {
      asyncBeans.add(new SyncToAsyncBeanDef<T>(syncBean));
    }

    return asyncBeans;
  }

  @Override
  public <T> Collection<AsyncBeanDef<T>> lookupBeans(final Class<T> type) {
    return wrapSyncBeans(innerBeanManager.lookupBeans(type));
  }

  @Override
  public <T> Collection<AsyncBeanDef<T>> lookupBeans(final Class<T> type, final Annotation... qualifiers) {
    return wrapSyncBeans(innerBeanManager.lookupBeans(type, qualifiers));
  }

  @Override
  public <T> AsyncBeanDef<T> lookupBean(Class<T> type, Annotation... qualifiers) {
    final IOCBeanDef<T> syncBean = innerBeanManager.lookupBean(type, qualifiers);

    return new SyncToAsyncBeanDef<T>(syncBean);
  }

  @Override
  public void setContextManager(final ContextManager contextManager) {
    innerBeanManager.setContextManager(contextManager);
  }

  public void reset() {
    innerBeanManager.reset();
  }

  public SyncBeanManager getInnerBeanManager() {
    return innerBeanManager;
  }
}
