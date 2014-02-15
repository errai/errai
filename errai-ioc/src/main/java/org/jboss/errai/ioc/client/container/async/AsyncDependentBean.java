package org.jboss.errai.ioc.client.container.async;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;

import javax.enterprise.context.Dependent;

import org.jboss.errai.common.client.util.CreationalCallback;

/**
 * @author Mike Brock
 */
public class AsyncDependentBean<T> extends AbstractAsyncBean<T> {
  protected final AsyncBeanManagerImpl beanManager;
  protected final AsyncBeanProvider<T> beanProvider;

  protected AsyncDependentBean(final AsyncBeanManagerImpl beanManager,
                               final Class<T> type,
                               final Class<?> beanType,
                               final Annotation[] qualifiers,
                               final String name,
                               final boolean concrete,
                               final AsyncBeanProvider<T> beanProvider) {
    this.beanManager = beanManager;
    this.type = type;
    this.beanType = beanType;

    if (qualifiers != null) {
      Collections.addAll(this.qualifiers = new HashSet<Annotation>(), qualifiers);
    }
    else {
      this.qualifiers = Collections.emptySet();
    }

    this.name = name;
    this.concrete = concrete;
    this.beanProvider = beanProvider;
  }

  public static <T> AsyncBeanDef<T> newBean(final AsyncBeanManagerImpl beanManager,
                                            final Class<T> type,
                                            final Class<?> beanType,
                                            final Annotation[] qualifiers,
                                            final String name,
                                            final boolean concrete,
                                            final AsyncBeanProvider<T> provider) {
    return new AsyncDependentBean<T>(beanManager, type, beanType, qualifiers, name, concrete, provider);
  }

  @Override
  public void newInstance(final CreationalCallback<T> callback) {
    final AsyncCreationalContext context = new AsyncCreationalContext(beanManager, Dependent.class);
    beanProvider.getInstance(new CreationalCallback<T>() {
      @Override
      public void callback(final T beanInstance) {
        context.finish(new Runnable() {
          @Override
          public void run() {
            callback.callback(beanInstance);
          }
        });
      }
    }, context);
  }

  @Override
  public void getInstance(final CreationalCallback<T> callback) {
    final AsyncCreationalContext context = new AsyncCreationalContext(beanManager, Dependent.class);
    getInstance(new CreationalCallback<T>() {
      @Override
      public void callback(final T beanInstance) {
        context.finish(new Runnable() {
          @Override
          public void run() {
            callback.callback(beanInstance);
          }
        });
      }
    }, context);
  }

  @Override
  public void getInstance(final CreationalCallback<T> callback, final AsyncCreationalContext context) {
    beanProvider.getInstance(callback, context);
  }

  @Override
  public Class<? extends Annotation> getScope() {
    return Dependent.class;
  }

  @Override
  public String toString() {
    return "AsyncDependentBean [name=" + name + ", type=" + type + ", beanType=" + beanType + ", qualifiers="
            + qualifiers + ", concrete=" + concrete + "]";
  }


}
