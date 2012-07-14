package org.jboss.errai.ioc.rebind.ioc.bootstrapper;

import org.jboss.errai.codegen.Context;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.meta.MetaClass;

/**
 * This is a marker interface to tell the IOCBootstrapGenerator that it should split out the wiring logic from this
 * point.
 *
 * @author Mike Brock
 */
public class SplitPoint implements Statement {
  @Override
  public String generate(Context context) {
    return "";
  }

  @Override
  public MetaClass getType() {
    return null;
  }
}
