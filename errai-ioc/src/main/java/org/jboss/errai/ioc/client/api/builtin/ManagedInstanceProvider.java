/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.ioc.client.api.builtin;

import static org.jboss.errai.ioc.client.QualifierUtil.DEFAULT_ANNOTATION;
import static org.jboss.errai.ioc.client.container.IOC.getBeanManager;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.enterprise.context.Dependent;

import org.jboss.errai.ioc.client.IOCUtil;
import org.jboss.errai.ioc.client.api.ContextualTypeProvider;
import org.jboss.errai.ioc.client.api.Disposer;
import org.jboss.errai.ioc.client.api.IOCProvider;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.jboss.errai.ioc.client.container.SyncBeanDef;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

@IOCProvider
public class ManagedInstanceProvider implements ContextualTypeProvider<ManagedInstance<?>>, Disposer<ManagedInstance<?>> {

  @Override
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public ManagedInstance<?> provide(final Class<?>[] typeargs, final Annotation[] qualifiers) {
    return new ManagedInstanceImpl(typeargs[0], qualifiers.length == 0 ? new Annotation[] { DEFAULT_ANNOTATION } : qualifiers);
  }

  @Override
  public void dispose(final ManagedInstance<?> managedInstance) {
    managedInstance.destroyAll();
  }

  private static class ManagedInstanceImpl<T> implements ManagedInstance<T> {

    private final Class<T> type;
    private final Set<Annotation> qualifiers;
    private final Multimap<Set<Annotation>, ? super T> dependentInstances;

    private ManagedInstanceImpl(final Class<T> type, final Annotation[] qualifiers) {
      this(type, qualifiers, ArrayListMultimap.create());
    }

    private ManagedInstanceImpl(final Class<T> type, final Annotation[] qualifiers, final Multimap<Set<Annotation>, ? super T> dependentInstances) {
      this(type, new HashSet<>(Arrays.asList(qualifiers)), dependentInstances);
    }

    private ManagedInstanceImpl(final Class<T> type, final Set<Annotation> qualifiers, final Multimap<Set<Annotation>, ? super T> dependentInstances) {
      this.type = type;
      this.qualifiers = qualifiers;
      this.dependentInstances = dependentInstances;
    }

    @Override
    public T get() {
      final SyncBeanDef<T> bean = IOCUtil.getSyncBean(type, qualifierArray());
      final T instance = bean.getInstance();
      if (Dependent.class.equals(bean.getScope())) {
        dependentInstances.put(qualifiers, instance);
      }

      return instance;
    }

    private Annotation[] qualifierArray() {
      return qualifiers.toArray(new Annotation[qualifiers.size()]);
    }

    @Override
    public Iterator<T> iterator() {
      return new ManagedInstanceImplIterator<T>(getBeanManager().lookupBeans(type, qualifierArray()), qualifiers, dependentInstances);
    }

    @Override
    public ManagedInstance<T> select(final Annotation... qualifiers) {
      return select(type, qualifiers);
    }

    @Override
    public <U extends T> ManagedInstance<U> select(final Class<U> subtype, final Annotation... qualifiers) {
      final Set<Annotation> combined = new HashSet<>(this.qualifiers);
      combined.addAll(Arrays.asList(qualifiers));
      return new ManagedInstanceImpl<>(subtype, combined, dependentInstances);
    }

    @Override
    public boolean isUnsatisfied() {
      return IOCUtil.isUnsatisfied(type, qualifierArray());
    }

    @Override
    public boolean isAmbiguous() {
      return IOCUtil.isAmbiguous(type, qualifierArray());
    }

    @Override
    public void destroy(final T instance) {
      IOCUtil.destroy(instance);
      dependentInstances.remove(qualifiers, instance);
    }

    @Override
    public void destroyAll() {
      final Iterator<Set<Annotation>> qualifiersIter = dependentInstances.keySet().iterator();
      while (qualifiersIter.hasNext()) {
        final Set<Annotation> quals = qualifiersIter.next();
        if (quals.containsAll(qualifiers)) {
          final Collection<? super T> instances = dependentInstances.get(quals);
          for (final Object instance : instances) {
            IOCUtil.destroy(instance);
          }
          qualifiersIter.remove();
        }
      }
    }

    private static class ManagedInstanceImplIterator<T> implements Iterator<T> {

      private final Iterator<SyncBeanDef<T>> delegate;
      private final Multimap<Set<Annotation>, ? super T> dependentInstances;
      private Object lastCreatedInstance;
      private final Set<Annotation> qualifiers;

      private ManagedInstanceImplIterator(final Collection<SyncBeanDef<T>> beanDefs, final Set<Annotation> qualifiers, final Multimap<Set<Annotation>, ? super T> dependentInstances) {
        this.qualifiers = qualifiers;
        this.delegate = beanDefs.iterator();
        this.dependentInstances = dependentInstances;
      }

      @Override
      public boolean hasNext() {
        return delegate.hasNext();
      }

      @Override
      public T next() {
        final SyncBeanDef<T> bean = delegate.next();
        final T instance = bean.getInstance();
        if (Dependent.class.equals(bean.getScope())) {
          dependentInstances.put(qualifiers, instance);
        }
        lastCreatedInstance = instance;
        return instance;
      }

      @Override
      public void remove() {
        if (lastCreatedInstance == null) {
          throw new IllegalStateException();
        }
        else if (dependentInstances.remove(qualifiers, lastCreatedInstance)) {
          IOCUtil.destroy(lastCreatedInstance);
          lastCreatedInstance = null;
        }
      }
    }

  }
}