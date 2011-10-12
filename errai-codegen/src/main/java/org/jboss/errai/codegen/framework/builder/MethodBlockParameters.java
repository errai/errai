package org.jboss.errai.codegen.framework.builder;

import org.jboss.errai.codegen.framework.DefParameters;
import org.jboss.errai.codegen.framework.meta.MetaClass;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface MethodBlockParameters<T> {
  public MethodBlockBuilder<T> parameters(DefParameters parms);

  public MethodBlockBuilder<T> parameters(Class<?>... parms);

  public MethodBlockBuilder<T> parameters(MetaClass... parms);

  public MethodBlockBuilder<T> parameters(Object... parms);
}
