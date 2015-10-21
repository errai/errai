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
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import javax.enterprise.inject.Alternative;

import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.ioc.client.QualifierUtil;
import org.jboss.errai.ioc.client.container.BeanManagerSetup;
import org.jboss.errai.ioc.client.container.BeanManagerUtil;
import org.jboss.errai.ioc.client.container.ContextManager;
import org.jboss.errai.ioc.client.container.DestructionCallback;
import org.jboss.errai.ioc.client.container.Factory;
import org.jboss.errai.ioc.client.container.FactoryHandle;
import org.jboss.errai.ioc.client.container.SyncBeanDef;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.jboss.errai.ioc.client.container.SyncBeanManagerImpl;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Alternative
public class AsyncBeanManagerImpl implements AsyncBeanManager, BeanManagerSetup, AsyncBeanManagerSetup {

  private final SyncBeanManagerImpl innerBeanManager = new SyncBeanManagerImpl();

  private final Multimap<String, String> typeNamesByName = HashMultimap.create();
  private final Multimap<String, UnloadedFactory> unloadedByTypeName = HashMultimap.create();

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

  // XXX Do we even need this?
  @Override
  public void destroyBean(Object ref, Runnable runnable) {
    // TODO Auto-generated method stub
    throw new RuntimeException("Not yet implemented.");
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public Collection<AsyncBeanDef> lookupBeans(String name) {
    final Collection syncBeans = innerBeanManager.lookupBeans(name);
    final Collection beans = wrapSyncBeans(syncBeans);
    addUnloadedBeans(beans, name);

    return beans;
  }

  private <T> Collection<AsyncBeanDef<T>> wrapSyncBeans(final Collection<SyncBeanDef<T>> syncBeans) {
    final Collection<AsyncBeanDef<T>> asyncBeans = new ArrayList<AsyncBeanDef<T>>(syncBeans.size());

    for (final SyncBeanDef<T> syncBean : syncBeans) {
      asyncBeans.add(new SyncToAsyncBeanDef<T>(syncBean));
    }

    return asyncBeans;
  }

  @Override
  public <T> Collection<AsyncBeanDef<T>> lookupBeans(final Class<T> type) {
    return lookupBeans(type, QualifierUtil.DEFAULT_ANNOTATION);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public <T> Collection<AsyncBeanDef<T>> lookupBeans(final Class<T> type, Annotation... qualifiers) {
    if (qualifiers.length == 0) {
      qualifiers = new Annotation[] { QualifierUtil.DEFAULT_ANNOTATION };
    }

    final Collection beans = wrapSyncBeans(innerBeanManager.lookupBeans(type, qualifiers));
    addUnloadedBeans(beans, type, type.getName(), qualifiers);

    return beans;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private <T> void addUnloadedBeans(final Collection<AsyncBeanDef> beans, final Class<T> type, final String typeName,
          final Annotation... qualifiers) {
    final Collection<UnloadedFactory> unloadedCandidates = unloadedByTypeName.get(typeName);
    final Collection<Annotation> allOf = Arrays.asList(qualifiers);
    for (final UnloadedFactory unloaded : unloadedCandidates) {
      if (QualifierUtil.matches(allOf, unloaded.getHandle().getQualifiers())) {
        final Class<T> beanType = (Class<T>) (type != null ? type : unloaded.getHandle().getActualType());
        beans.add(new FactoryLoaderBeanDef<T>(beanType, unloaded.getHandle(), (FactoryLoader<T>) unloaded.getLoader()));
      }
    }
  }

  @SuppressWarnings("rawtypes")
  private void addUnloadedBeans(final Collection<AsyncBeanDef> beans, final String name) {
    for (final String typeName : typeNamesByName.get(name)) {
      addUnloadedBeans(beans, null, typeName, QualifierUtil.DEFAULT_ANNOTATION);
    }
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public <T> AsyncBeanDef<T> lookupBean(Class<T> type, Annotation... qualifiers) {
    final Collection beans = lookupBeans(type, qualifiers);

    if (beans.size() > 1) {
      throw BeanManagerUtil.unsatisfiedResolutionException(type, qualifiers);
    } else if (beans.isEmpty()) {
      throw BeanManagerUtil.ambiguousResolutionException(type, beans, qualifiers);
    } else {
      return (AsyncBeanDef<T>) beans.iterator().next();
    }
  }

  @Override
  public void setContextManager(final ContextManager contextManager) {
    innerBeanManager.setContextManager(contextManager);
  }

  public void reset() {
    typeNamesByName.clear();
    unloadedByTypeName.clear();
    innerBeanManager.reset();
  }

  public SyncBeanManager getInnerBeanManager() {
    return innerBeanManager;
  }

  @Override
  public void registerAsyncBean(final FactoryHandle handle, final FactoryLoader<?> future) {
    final String beanName;
    if (handle.getBeanName() != null) {
      beanName = handle.getBeanName();
    } else {
      beanName = handle.getActualType().getName();
    }

    typeNamesByName.put(beanName, handle.getActualType().getName());

    final UnloadedFactory unloaded = new UnloadedFactory(handle, future);
    for (final Class<?> assignable : handle.getAssignableTypes()) {
      unloadedByTypeName.put(assignable.getName(), unloaded);
    }
  }

  private static class UnloadedFactory {
    private final FactoryHandle handle;
    private final FactoryLoader<?> loader;

    public UnloadedFactory(final FactoryHandle handle, final FactoryLoader<?> loader) {
      this.handle = handle;
      this.loader = loader;
    }

    public FactoryHandle getHandle() {
      return handle;
    }

    public FactoryLoader<?> getLoader() {
      return loader;
    }
  }

  private class FactoryLoaderBeanDef<T> implements AsyncBeanDef<T> {

    boolean isLoaded = false;
    private final Class<T> type;
    private final FactoryHandle handle;
    private final FactoryLoader<T> loader;

    public FactoryLoaderBeanDef(final Class<T> type, final FactoryHandle handle, final FactoryLoader<T> loader) {
      this.type = type;
      this.handle = handle;
      this.loader = loader;
    }

    @Override
    public Class<T> getType() {
      return type;
    }

    @Override
    public Class<?> getBeanClass() {
      return handle.getActualType();
    }

    @Override
    public Class<? extends Annotation> getScope() {
      return handle.getScope();
    }

    @Override
    public void getInstance(final CreationalCallback<T> callback) {
      if (!isLoaded) {
        loader.call(new FactoryLoaderCallback<T>() {
          @Override
          public void callback(final Factory<T> factory) {
            // TODO remove unloadedFactory
            innerBeanManager.addFactory(factory);
            isLoaded = true;
            final T instance = performSyncLookup().getInstance();
            callback.callback(instance);
          }
        });
      } else {
        final T instance = performSyncLookup().getInstance();
        callback.callback(instance);
      }
    }

    @Override
    public void newInstance(final CreationalCallback<T> callback) {
      if (!isLoaded) {
        loader.call(new FactoryLoaderCallback<T>() {
          @Override
          public void callback(final Factory<T> factory) {
            innerBeanManager.addFactory(factory);
            isLoaded = true;
            final T instance = performSyncLookup().newInstance();
            callback.callback(instance);
          }
        });
      } else {
        final T instance = performSyncLookup().newInstance();
        callback.callback(instance);
      }
    }

    private SyncBeanDef<T> performSyncLookup() {
      return innerBeanManager.lookupBean(type, handle.getQualifiers().toArray(new Annotation[0]));
    }

    @Override
    public Set<Annotation> getQualifiers() {
      return handle.getQualifiers();
    }

    @Override
    public boolean matches(final Set<Annotation> annotations) {
      return QualifierUtil.matches(handle.getQualifiers(), annotations);
    }

    @Override
    public String getName() {
      return handle.getBeanName();
    }

    @Override
    public boolean isConcrete() {
      return true;
    }

    @Override
    public boolean isActivated() {
      return true;
    }

  }
}
