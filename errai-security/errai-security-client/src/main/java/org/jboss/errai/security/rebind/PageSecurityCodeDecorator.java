package org.jboss.errai.security.rebind;

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.InitializationCallback;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.security.client.local.nav.PageAuthenticationLifecycleListener;
import org.jboss.errai.security.client.local.nav.PageRoleLifecycleListener;
import org.jboss.errai.security.shared.RequireAuthentication;
import org.jboss.errai.security.shared.RequireRoles;
import org.jboss.errai.ui.nav.client.local.Page;

/**
 * Register page lifecycle listeners when {@linkplain Page pages} are created.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@CodeDecorator
public class PageSecurityCodeDecorator extends IOCDecoratorExtension<Page> {

  public PageSecurityCodeDecorator(Class<Page> decoratesWith) {
    super(decoratesWith);
  }

  @Override
  public List<? extends Statement> generateDecorator(InjectableInstance<Page> ctx) {
    final List<Statement> stmts = new ArrayList<Statement>();

    if (ctx.getInjector().getInjectedType().isAnnotationPresent(RequireAuthentication.class)) {
      ctx.getTargetInjector().addStatementToEndOfInjector(
              Stmt.loadVariable("context").invoke("addInitializationCallback",
                      Refs.get(ctx.getInjector().getInstanceVarName()),
                      createInitializationCallback(
                              ctx,
                              Stmt.invokeStatic(IOC.class, "registerLifecycleListener",
                                      Refs.get(ctx.getInjector().getInstanceVarName()),
                                      Stmt.create().newObject(PageAuthenticationLifecycleListener.class)))));
    }
    if (ctx.getInjector().getInjectedType().isAnnotationPresent(RequireRoles.class)) {
      final RequireRoles annotation = ctx.getAnnotation(RequireRoles.class);
      ctx.getTargetInjector().addStatementToEndOfInjector(
              Stmt.loadVariable("context")
                      .invoke("addInitializationCallback",
                              Refs.get(ctx.getInjector().getInstanceVarName()),
                              createInitializationCallback(
                                      ctx,
                                      Stmt.invokeStatic(
                                              IOC.class,
                                              "registerLifecycleListener",
                                              Refs.get(ctx.getInjector().getInstanceVarName()),
                                              Stmt.newObject(PageRoleLifecycleListener.class,
                                                      (Object[]) annotation.value())))));

    }

    return stmts;
  }

  private Statement createInitializationCallback(final InjectableInstance<Page> ctx, final Statement... statements) {
    BlockBuilder<AnonymousClassStructureBuilder> callbackMethod =
            Stmt.newObject(
                    MetaClassFactory.parameterizedAs(InitializationCallback.class,
                            MetaClassFactory.typeParametersOf(ctx.getInjector().getInjectedType())))
                    .extend()
                    .publicOverridesMethod(
                            "init",
                            Parameter.finalOf(ctx.getInjector().getInjectedType(), "obj"));

    for (int i = 0; i < statements.length; i++) {
      callbackMethod = callbackMethod.append(statements[i]);
    }

    return callbackMethod.finish().finish();
  }
}
