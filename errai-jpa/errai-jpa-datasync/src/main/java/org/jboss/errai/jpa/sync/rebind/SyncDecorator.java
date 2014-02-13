package org.jboss.errai.jpa.sync.rebind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.client.container.InitializationCallback;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.injector.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.jpa.sync.client.local.ClientSyncWorker;
import org.jboss.errai.jpa.sync.client.local.DataSyncCallback;
import org.jboss.errai.jpa.sync.client.local.Sync;
import org.jboss.errai.jpa.sync.client.local.SyncParam;
import org.jboss.errai.jpa.sync.client.shared.SyncResponses;

import static org.jboss.errai.codegen.meta.MetaClassFactory.*;

/**
 * Generates an {@link InitializationCallback} that contains data sync logic.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
@CodeDecorator
public class SyncDecorator extends IOCDecoratorExtension<Sync> {

  public SyncDecorator(Class<Sync> decoratesWith) {
    super(decoratesWith);
  }

  @Override
  public List<? extends Statement> generateDecorator(InjectableInstance<Sync> ctx) {

    final List<Statement> statements = new ArrayList<Statement>();
    statements.add(Stmt.codeComment("begin SyncDecorator code"));

    // Ensure private accessor is generated for the sync method
//    ctx.ensureMemberExposed();
    Sync syncAnnotation = ctx.getAnnotation();
    for (SyncParam param : syncAnnotation.params()) {
      statements.add(Stmt.codeComment(param.toString()));
    }
    statements.add(Stmt.declareFinalVariable("paramsMap", Map.class, Stmt.invokeStatic(Collections.class, "emptyMap")));
    statements.add(Stmt.declareFinalVariable("objectClass", Class.class, Stmt.loadLiteral(Object.class)));

    statements.add(Stmt.declareFinalVariable(
            "syncWorker",
            ClientSyncWorker.class,
            Stmt.invokeStatic(ClientSyncWorker.class, "create", syncAnnotation.query(),
                    Stmt.loadVariable("objectClass"), Stmt.loadVariable("paramsMap"), null)));

    statements.add(Stmt.loadVariable("syncWorker").invoke("addSyncCallback", createSyncCallback(ctx)));

    ctx.getTargetInjector().addStatementToEndOfInjector(
        Stmt.loadVariable("context").invoke("addInitializationCallback",
                  Refs.get(ctx.getInjector().getInstanceVarName()),
                  createInitCallback(ctx.getEnclosingType(), "obj")));

    Statement destruction = Stmt.loadVariable("syncWorker").invoke("stop");
    ctx.getTargetInjector().addStatementToEndOfInjector(
            Stmt.loadVariable("context").invoke(
                    "addDestructionCallback",
                    Refs.get(ctx.getInjector().getInstanceVarName()),
                    InjectUtil.createDestructionCallback(ctx.getEnclosingType(), "obj",
                            Collections.singletonList(destruction))));

    statements.add(Stmt.codeComment("end SyncDecorator code"));
    return statements;

  }

  /**
   * Generates an anonymous {@link DataSyncCallback} that will invoke the decorated sync method.
   */
  private Statement createSyncCallback(InjectableInstance<Sync> ctx) {
    return
        Stmt.newObject(DataSyncCallback.class)
            .extend()
            .publicOverridesMethod("onSync", Parameter.of(SyncResponses.class, "responses", true))
            .append(ctx.callOrBind(Stmt.loadVariable("responses")))
            .finish()
            .finish();
  }

  /**
   * Generates an anonymous {@link InitializationCallback} that will contain the logic to start the {@link ClientSyncWorker}.
   */
  private Statement createInitCallback(final MetaClass type, final String initVar) {
    return
        Stmt.newObject(parameterizedAs(InitializationCallback.class, typeParametersOf(type)))
            .extend()
            .publicOverridesMethod("init", Parameter.of(type, initVar, true))
            .append(Stmt.loadVariable("syncWorker").invoke("start"))
            .finish()
            .finish();
  }

}
