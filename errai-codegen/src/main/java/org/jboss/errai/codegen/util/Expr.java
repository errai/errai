package org.jboss.errai.codegen.util;

import org.jboss.errai.codegen.Context;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.meta.MetaClass;

/**
 * @author Mike Brock
 */
public class Expr {
  public static Statement qualify(final Statement statement) {
    return new Statement() {
      @Override
      public String generate(Context context) {
        return "(" + statement.generate(context) + ")";
      }

      @Override
      public MetaClass getType() {
        return statement.getType();
      }
    };
  }
}
