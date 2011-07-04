package org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl;

import org.jboss.errai.ioc.rebind.ioc.codegen.Modifier;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.BlockBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.MethodBlockModifiers;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface MethodBlockBuilder<T> extends BlockBuilder<T>, MethodBlockModifiers<MethodBlockBuilder<T>, T> {
  public BlockBuilder<T> throws_(Class<? extends Throwable>... exceptionTypes);

  public BlockBuilder<T> throws_(MetaClass... exceptions);

  @Override
  public MethodBlockBuilder<T> modifiers(Modifier... modifiers);

  @Override
  public BlockBuilder<T> body();

  @Override
  public T finish();
}
