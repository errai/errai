package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.Modifier;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface MethodBlockModifiers<B, T> {
  public B modifiers(Modifier... modifiers);

  public BlockBuilder<T> body();
}
