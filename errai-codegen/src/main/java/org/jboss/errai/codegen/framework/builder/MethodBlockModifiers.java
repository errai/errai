package org.jboss.errai.codegen.framework.builder;

import org.jboss.errai.codegen.framework.Modifier;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface MethodBlockModifiers<B, T> {
  public B modifiers(Modifier... modifiers);

  public BlockBuilder<T> body();
}
