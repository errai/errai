/*
 * Copyright 2012 JBoss, by Red Hat, Inc
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

import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.ioc.client.container.IOC;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An <tt>AsyncBeanQuery</tt> is used for querying more than one bean at a time, and then orchestrating
 * a unit of work to happen only after all of the beans in the query have been successfully loaded.
 *
 * @author Mike Brock
 */
public class AsyncBeanQuery {
  private Runnable finishCallback;

  private final List<LoadStrategy> loadStrategies
      = new ArrayList<LoadStrategy>();

  private final Set<LoadStrategy> loaded
      = new HashSet<LoadStrategy>();

  public <T> AsyncBeanFuture<T> load(final Class<T> type) {
    return load(IOC.getAsyncBeanManager().lookupBean(type));
  }

  public <T> AsyncBeanFuture<T> load(final Class<T> type, final Annotation... qualifiers) {
    return load(IOC.getAsyncBeanManager().lookupBean(type, qualifiers));
  }

  public <T> AsyncBeanFuture<T> load(final AsyncBeanDef<T> beanDef) {
    return load(beanDef, false);
  }

  public <T> AsyncBeanFuture<T> loadNew(final Class<T> type) {
    return loadNew(IOC.getAsyncBeanManager().lookupBean(type));
  }

  public <T> AsyncBeanFuture<T> loadNew(final Class<T> type, final Annotation... qualifiers) {
    return loadNew(IOC.getAsyncBeanManager().lookupBean(type, qualifiers));
  }

  public <T> AsyncBeanFuture<T> loadNew(final AsyncBeanDef<T> beanDef) {
    return load(beanDef, true);
  }

  private <T> AsyncBeanFuture<T> load(final AsyncBeanDef<T> beanDef, final boolean loadNew) {
    final LoadStrategy<T> loadStrategy = new LoadStrategy<T>(beanDef, loadNew);
    loadStrategies.add(loadStrategy);
    return loadStrategy.getFuture();
  }

  public void query(final Runnable finishCallback) {
    Assert.notNull(finishCallback);

    this.finishCallback = finishCallback;

    for (final LoadStrategy strategy : loadStrategies) {
      strategy.load();
    }
  }

  private void vote(LoadStrategy<?> strategy) {
    loaded.add(strategy);
    if (loaded.containsAll(loadStrategies)) {
      finishCallback.run();
    }
  }

  private class LoadStrategy<T> {
    private final AsyncBeanDef<T> beanDef;
    private final AsyncBeanFuture<T> future;
    private final boolean loadNew;

    private LoadStrategy(final AsyncBeanDef<T> beanDef, final boolean loadNew) {
      this.beanDef = beanDef;
      this.loadNew = loadNew;

      this.future = new AsyncBeanFuture<T>(new CreationalCallback<T>() {
        @Override
        public void callback(T beanInstance) {
          vote(LoadStrategy.this);
        }
      });
    }


    public AsyncBeanDef<T> getBeanDef() {
      return beanDef;
    }

    public AsyncBeanFuture<T> getFuture() {
      return future;
    }

    public boolean isLoadNew() {
      return loadNew;
    }

    public void load() {
      if (loadNew) {
        beanDef.newInstance(future.getCreationalCallback());
      }
      else {
        beanDef.getInstance(future.getCreationalCallback());
      }
    }
  }
}
