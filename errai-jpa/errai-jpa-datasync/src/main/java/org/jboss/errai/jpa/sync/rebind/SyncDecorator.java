/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.jpa.sync.rebind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.exception.GenerationException;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.util.GenUtil;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.client.container.InitializationCallback;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.injector.api.Decorable;
import org.jboss.errai.ioc.rebind.ioc.injector.api.FactoryController;
import org.jboss.errai.jpa.sync.client.local.ClientSyncWorker;
import org.jboss.errai.jpa.sync.client.local.ClientSyncWorker.QueryParamInitCallback;
import org.jboss.errai.jpa.sync.client.local.DataSyncCallback;
import org.jboss.errai.jpa.sync.client.local.Sync;
import org.jboss.errai.jpa.sync.client.local.SyncParam;
import org.jboss.errai.jpa.sync.client.shared.SyncResponses;

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
  public void generateDecorator(final Decorable decorable, final FactoryController controller) {

    MetaMethod method = decorable.getAsMethod();
    MetaParameter[] params = method.getParameters();
    if (params.length != 1 || !params[0].getType().getErased().equals(MetaClassFactory.get(SyncResponses.class))) {
      throw new GenerationException("Methods annotated with @" + Sync.class.getName()
          + " need to have exactly one parameter of type: "
          + SyncResponses.class.getName() +
          ". Check method: " + GenUtil.getMethodString(method) + " in class "
          + method.getDeclaringClass().getFullyQualifiedName());
    }

    Sync syncAnnotation = (Sync) decorable.getAnnotation();

    controller.addInitializationStatements(createInitStatements(decorable.getDecorableDeclaringType(), "obj", syncAnnotation, decorable, controller));

    final Statement syncWorker = controller.getReferenceStmt("syncWorker", ClientSyncWorker.class);
    final Statement destruction = Stmt.nestedCall(syncWorker).invoke("stop");
    controller.addDestructionStatements(Collections.singletonList(destruction));
  }

  /**
   * Generates an anonymous {@link DataSyncCallback} that will invoke the decorated sync method.
   */
  private Statement createSyncCallback(Decorable decorable) {
    return Stmt.newObject(DataSyncCallback.class)
            .extend()
            .publicOverridesMethod("onSync", Parameter.of(SyncResponses.class, "responses", true))
            .append(decorable.getAccessStatement(Stmt.loadVariable("responses")))
            .finish()
            .finish();
  }

  private List<Statement> createInitStatements(final MetaClass type, final String initVar, final Sync syncAnnotation,
      final Decorable decorable, final FactoryController controller) {

    final List<Statement> statements = new ArrayList<Statement>();

    statements.add(Stmt.declareFinalVariable("objectClass", Class.class, Stmt.loadLiteral(Object.class)));

    BlockBuilder<AnonymousClassStructureBuilder> queryParamCallback =
        Stmt.newObject(QueryParamInitCallback.class).extend().publicOverridesMethod("getQueryParams");

    queryParamCallback.append(Stmt.declareFinalVariable("paramsMap", Map.class, Stmt.newObject(HashMap.class)));

    for (SyncParam param : syncAnnotation.params()) {
      Statement fieldValueStmt;
      String val = param.val().trim();
      if (val.startsWith("{") && val.endsWith("}")) {
        String fieldName = val.substring(1, val.length() - 1);
        MetaField field = decorable.getDecorableDeclaringType().getInheritedField(fieldName);
        fieldValueStmt =
            InjectUtil.getPublicOrPrivateFieldValue(controller, field);
      }
      else {
        fieldValueStmt = Stmt.loadLiteral(val);
      }
      queryParamCallback.append(Stmt.loadVariable("paramsMap").invoke("put", param.name(), fieldValueStmt));
    }
    queryParamCallback.append(Stmt.loadVariable("paramsMap").returnValue());

    statements.add(Stmt.declareFinalVariable("paramsCallback",
        QueryParamInitCallback.class, queryParamCallback.finish().finish()));

    statements.add(controller.setReferenceStmt("syncWorker", Stmt.invokeStatic(ClientSyncWorker.class, "create",
            syncAnnotation.query(), Stmt.loadVariable("objectClass"), null)));

    final Statement syncWorkerRef = controller.getReferenceStmt("syncWorker", ClientSyncWorker.class);
    statements.add(
        Stmt.nestedCall(syncWorkerRef).invoke("addSyncCallback", createSyncCallback(decorable)));
    statements.add(
            Stmt.nestedCall(syncWorkerRef).invoke("start", Refs.get("instance"), Stmt.loadVariable("paramsCallback")));

    return statements;
  }

}
