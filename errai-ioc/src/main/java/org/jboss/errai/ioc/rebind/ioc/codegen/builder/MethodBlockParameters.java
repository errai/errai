package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.DefParameters;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface MethodBlockParameters<T> {
  public MethodBlockBuilder<T> parameters(DefParameters parms);

  public MethodBlockBuilder<T> parameters(Class<T>... parms);

  public MethodBlockBuilder<T> parameters(MetaClass... parms);
}
