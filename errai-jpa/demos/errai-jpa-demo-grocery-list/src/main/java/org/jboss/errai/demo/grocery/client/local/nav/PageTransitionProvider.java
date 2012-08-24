package org.jboss.errai.demo.grocery.client.local.nav;

import java.lang.annotation.Annotation;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.errai.demo.grocery.client.local.Navigation;
import org.jboss.errai.ioc.client.api.ContextualTypeProvider;
import org.jboss.errai.ioc.client.api.IOCProvider;

@IOCProvider @Singleton
public class PageTransitionProvider implements ContextualTypeProvider<PageTransition<?>> {

  @Inject Navigation navigation;

  @Override
  public PageTransition<?> provide(Class<?>[] typeargs, Annotation[] qualifiers) {
    @SuppressWarnings("unchecked")
    Class<Page> toPageType = (Class<Page>) typeargs[0];

    return new PageTransition<Page>(navigation, toPageType);
  }

}
