package org.jboss.errai.ui.nav.client.local;

import java.lang.annotation.Annotation;

import javax.inject.Singleton;

import org.jboss.errai.ioc.client.api.ContextualTypeProvider;
import org.jboss.errai.ioc.client.api.IOCProvider;

import com.google.gwt.user.client.ui.Widget;

@IOCProvider @Singleton
public class PageTransitionProvider implements ContextualTypeProvider<TransitionTo<?>> {

  @Override
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public TransitionTo provide(Class<?>[] typeargs, Annotation[] qualifiers) {
    Class<Widget> toPageType = (Class<Widget>) typeargs[0];
    return new TransitionTo<Widget>(toPageType);
  }

}
