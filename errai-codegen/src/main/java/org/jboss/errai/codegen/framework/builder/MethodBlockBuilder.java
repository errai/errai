package org.jboss.errai.codegen.framework.builder;

import org.jboss.errai.codegen.framework.meta.MetaClass;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface MethodBlockBuilder<T> extends BlockBuilder<T>,
                                               MethodBlockModifiers<MethodBlockBuilder<T>, T>,
                                               MethodBlockParameters<T> {
  public BlockBuilder<T> throws_(Class<? extends Throwable>... exceptionTypes);

  public BlockBuilder<T> throws_(MetaClass... exceptions);

//  @Override
//  public MethodBlockBuilder<T> modifiers(Modifier... modifiers);

  @Override
  public BlockBuilder<T> body();

  @Override
  public T finish();
}
