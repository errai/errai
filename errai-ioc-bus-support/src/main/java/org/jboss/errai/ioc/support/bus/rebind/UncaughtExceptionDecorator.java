/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.ioc.support.bus.rebind;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.UncaughtException;
import org.jboss.errai.bus.client.framework.ClientMessageBusImpl;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.exception.GenerationException;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.util.GenUtil;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.client.container.Factory;
import org.jboss.errai.ioc.client.container.InitializationCallback;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.injector.api.Decorable;
import org.jboss.errai.ioc.rebind.ioc.injector.api.FactoryController;

import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;

/**
 * Generates an {@link InitializationCallback} that registers an {@link UncaughtExceptionHandler}
 * with the client side message bus.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@CodeDecorator
public class UncaughtExceptionDecorator extends IOCDecoratorExtension<UncaughtException> {

  public UncaughtExceptionDecorator(Class<UncaughtException> decoratesWith) {
    super(decoratesWith);
  }

  @Override
  public void generateDecorator(final Decorable decorable, final FactoryController controller) {
    // Ensure that method has exactly one parameter of type Throwable
    final MetaMethod method = decorable.getAsMethod();
    MetaParameter[] parms = method.getParameters();
    if (!(parms.length == 1 && parms[0].getType().equals(MetaClassFactory.get(Throwable.class)))) {
      throw new GenerationException("Methods annotated with " + UncaughtException.class.getName()
          + " must have exactly one parameter of type " + Throwable.class.getName()
          + ". Invalid parameters in method: "
          + GenUtil.getMethodString(method) + " of type " + method.getDeclaringClass() + ".");
    }

    final boolean isEnclosingTypeDependent = decorable.isEnclosingTypeDependent();
    final String handlerVar = method.getName() + "Handler";
    final Statement setRefStmt = controller.setReferenceStmt(handlerVar, Refs.get(handlerVar));
    final Statement getRefStmt = controller.getReferenceStmt(handlerVar, UncaughtExceptionHandler.class);

    final List<Statement> initStatements = new ArrayList<Statement>(Arrays.asList(
            generateExceptionHandler(decorable, controller, handlerVar, isEnclosingTypeDependent),
            Stmt.castTo(ClientMessageBusImpl.class, Stmt.invokeStatic(ErraiBus.class, "get"))
                    .invoke("addUncaughtExceptionHandler", Refs.get(handlerVar))));
    if (isEnclosingTypeDependent) {
      initStatements.add(setRefStmt);
    }

    final List<Statement> destStatements = Arrays.<Statement>asList(
            Stmt.castTo(ClientMessageBusImpl.class, Stmt.invokeStatic(ErraiBus.class, "get"))
                    .invoke("removeUncaughtExceptionHandler", getRefStmt));

    if (isEnclosingTypeDependent) {
      controller.addInitializationStatements(initStatements);
      controller.addDestructionStatements(destStatements);
    } else {
      controller.addFactoryInitializationStatements(initStatements);
    }
  }

  private Statement generateExceptionHandler(final Decorable decorable, final FactoryController controller,
          final String name, final boolean isEnclosingTypeDependent) {
    final BlockBuilder<AnonymousClassStructureBuilder> initBuilder = Stmt.newObject(UncaughtExceptionHandler.class)
            .extend().publicMethod(void.class, "onUncaughtException", Parameter.of(Throwable.class, "t")).body();
    if (!isEnclosingTypeDependent) {
      final MetaClass enclosingType = decorable.getEnclosingInjectable().getInjectedType();
      initBuilder.append(Stmt.declareFinalVariable("instance", enclosingType,
              Stmt.castTo(enclosingType, Stmt.invokeStatic(Factory.class, "maybeUnwrapProxy", controller.contextGetInstanceStmt()))));
    }

    final ObjectBuilder initStmt = initBuilder.append(decorable.call(Refs.get("t"))).finish().finish();

    final Statement handlerStatement =
        Stmt.declareFinalVariable(name, UncaughtExceptionHandler.class,
            initStmt);

    return handlerStatement;
  }
}
