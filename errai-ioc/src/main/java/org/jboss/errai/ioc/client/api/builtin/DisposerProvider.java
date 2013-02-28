package org.jboss.errai.ioc.client.api.builtin;

import org.jboss.errai.ioc.client.api.ContextualTypeProvider;
import org.jboss.errai.ioc.client.api.Disposer;
import org.jboss.errai.ioc.client.api.EnabledByProperty;
import org.jboss.errai.ioc.client.api.IOCProvider;
import org.jboss.errai.ioc.client.container.SyncBeanManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;

/**
 * @author Mike Brock
 */
@IOCProvider
@Singleton
@EnabledByProperty(value = "errai.ioc.async_bean_manager", negated = true)
public class DisposerProvider implements ContextualTypeProvider<Disposer> {
  @Inject
  SyncBeanManager beanManager;

  @Override
  public Disposer provide(final Class<?>[] typeArguments, final Annotation[] qualifiers) {
    return new Disposer() {
      @Override
      public void dispose(final Object beanInstance) {
        beanManager.destroyBean(beanInstance);
      }
    };
  }
}
