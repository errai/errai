package org.jboss.errai.demo.grocery.client.local.nav;

import java.lang.annotation.Annotation;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.errai.demo.grocery.client.local.Navigation;
import org.jboss.errai.ioc.client.api.ContextualTypeProvider;
import org.jboss.errai.ioc.client.api.IOCProvider;
import org.jboss.errai.ioc.client.container.IOCBeanManager;

import com.google.gwt.user.client.ui.Widget;

@IOCProvider @Singleton
public class PageTransitionProvider implements ContextualTypeProvider<PageTransition> {

  @Inject Navigation navigation;
  @Inject IOCBeanManager bm;

  @Override
  public PageTransition provide(Class<?>[] typeargs, Annotation[] qualifiers) {
    // FIXME we don't get the field name or "fromPage" instance here
    return new PageTransition<Page>(navigation, new Page() {

      @Override
      public String name() {
        return "Dummy Page";
      }

      @Override
      public Widget content() {
        return null;
      }
    }, (Page) bm.lookupBean(typeargs[0], qualifiers).getInstance(), "Action");
  }

}
