package org.jboss.errai.ioc.tests.decorator.rebind;

import java.util.Arrays;

import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.injector.api.Decorable;
import org.jboss.errai.ioc.rebind.ioc.injector.api.FactoryController;
import org.jboss.errai.ioc.tests.decorator.client.res.LogCall;
import org.jboss.errai.ioc.tests.decorator.client.res.TestDataCollector;

/**
 * @author Mike Brock
 */

@CodeDecorator
public class LogCallDecorator extends IOCDecoratorExtension<LogCall> {
  public LogCallDecorator(Class<LogCall> decoratesWith) {
    super(decoratesWith);
  }

  @Override
  public void generateDecorator(Decorable decorable, FactoryController controller) {
    controller.addInvokeBefore(decorable.getAsMethod(),
        Stmt.invokeStatic(TestDataCollector.class, "beforeInvoke", Refs.get("text"), Refs.get("blah")));

    controller.addInvokeAfter(decorable.getAsMethod(),
        Stmt.invokeStatic(TestDataCollector.class, "afterInvoke", Refs.get("text"), Refs.get("blah")));

    final Statement foobar
        = controller.addProxyProperty("foobar", String.class, Stmt.load("foobie!"));

    controller.addInvokeAfter(decorable.getAsMethod(),
        Stmt.invokeStatic(TestDataCollector.class, "property", "foobar", foobar)
    );

    controller.addInitializationStatements(Arrays.<Statement>asList(Stmt.loadVariable("instance").invoke("setFlag", true)));
    controller.addDestructionStatements(Arrays.<Statement>asList(Stmt.loadVariable("instance").invoke("setFlag", false)));
  }
}
