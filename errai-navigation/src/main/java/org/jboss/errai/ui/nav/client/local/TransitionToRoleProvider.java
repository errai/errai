package org.jboss.errai.ui.nav.client.local;

import java.lang.annotation.Annotation;

import javax.inject.Singleton;

import org.jboss.errai.ioc.client.api.ContextualTypeProvider;
import org.jboss.errai.ioc.client.api.IOCProvider;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
@IOCProvider @Singleton
public class TransitionToRoleProvider implements ContextualTypeProvider<TransitionToRole<?>> {

  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public TransitionToRole provide(final Class<?>[] typeargs, final Annotation[] qualifiers) {
    final Class<UniquePageRole> uniquePageRole = (Class<UniquePageRole>) typeargs[0];
    return new TransitionToRole<UniquePageRole>(uniquePageRole);
  }

}
