package org.jboss.errai.ioc.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;

/**
 * @author Mike Brock
 */
public final class RunAsyncWrapper {
  private RunAsyncWrapper() {}

  public static Statement wrap(Statement statement) {
    return Stmt.invokeStatic(GWT.class, "runAsync", ObjectBuilder.newInstanceOf(RunAsyncCallback.class).extend()
            .publicOverridesMethod("onFailure", Parameter.of(Throwable.class, "throwable"))
            .append(Stmt.throw_(RuntimeException.class, "failed to run asynchronously", Refs.get("throwable")))
            .finish()

            .publicOverridesMethod("onSuccess")
            .append(statement).finish().finish());
  }
}
