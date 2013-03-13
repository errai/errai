package org.jboss.errai.ioc.tests.decorator.rebind;

import org.jboss.errai.codegen.ProxyMaker;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ioc.tests.decorator.client.res.LogCall;
import org.jboss.errai.ioc.tests.decorator.client.res.TestDataCollector;

import java.util.Collections;
import java.util.List;

/**
 * @author Mike Brock
 */

@CodeDecorator
public class LogCallDecorator extends IOCDecoratorExtension<LogCall> {
  public LogCallDecorator(Class<LogCall> decoratesWith) {
    super(decoratesWith);
  }

  @Override
  public List<? extends Statement> generateDecorator(InjectableInstance<LogCall> ctx) {
    ctx.getInjector().addInvokeBefore(ctx.getMethod(),
        Stmt.invokeStatic(TestDataCollector.class, "beforeInvoke", Refs.get("a0"), Refs.get("a1")));

    ctx.getInjector().addInvokeAfter(ctx.getMethod(),
        Stmt.invokeStatic(TestDataCollector.class, "afterInvoke", Refs.get("a0"), Refs.get("a1")));

    final ProxyMaker.ProxyProperty foobar
        = ctx.getInjector().addProxyProperty("foobar", String.class, Stmt.load("foobie!"));

    ctx.getInjector().addInvokeAfter(ctx.getMethod(),
      Stmt.invokeStatic(TestDataCollector.class, "property", "foobar", foobar)
    );

    return Collections.emptyList();
  }
}
