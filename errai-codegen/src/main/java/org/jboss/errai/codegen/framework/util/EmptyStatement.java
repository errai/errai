package org.jboss.errai.codegen.framework.util;

import org.jboss.errai.codegen.framework.Context;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */                         
public class EmptyStatement implements Statement {
  public static final Statement INSTANCE = new EmptyStatement();
  
  @Override
  public String generate(Context context) {
    return "";
  }

  @Override
  public MetaClass getType() {
    return MetaClassFactory.get(void.class);
  }
}
