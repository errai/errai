/*
 * Copyright 2013 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ioc.support.bus.rebind;

import static org.jboss.errai.codegen.meta.MetaClassFactory.parameterizedAs;
import static org.jboss.errai.codegen.meta.MetaClassFactory.typeParametersOf;

import java.util.Collections;
import java.util.List;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.UncaughtException;
import org.jboss.errai.bus.client.framework.ClientMessageBusImpl;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.exception.GenerationException;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.util.GenUtil;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.client.container.InitializationCallback;
import org.jboss.errai.ioc.client.container.RefHolder;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.injector.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;

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
  public List<? extends Statement> generateDecorator(InjectableInstance<UncaughtException> ctx) {
    ctx.ensureMemberExposed();

    // Ensure that method has exactly one parameter of type Throwable
    MetaParameter[] parms = ctx.getMethod().getParameters();
    if (!(parms.length == 1 && parms[0].getType().equals(MetaClassFactory.get(Throwable.class)))) {
      throw new GenerationException("Methods annotated with " + UncaughtException.class.getName()
          + " must have exactly one parameter of type " + Throwable.class.getName()
          + ". Invalid parameters in method: "
          + GenUtil.getMethodString(ctx.getMethod()) + " of type " + ctx.getMethod().getDeclaringClass() + ".");
    }

    // Generate a RefHolder so we can access the UncaughtExceptionHandler instance in both the
    // initialization and destruction callback of the bean.
    final MetaClass holderType = MetaClassFactory.parameterizedAs(RefHolder.class,
        MetaClassFactory.typeParametersOf(UncaughtExceptionHandler.class));
    final String holderVar = InjectUtil.getUniqueVarName();

    ctx.getTargetInjector().addStatementToEndOfInjector(
        Stmt.declareFinalVariable(holderVar, holderType, Stmt.newObject(holderType)));

    // Generate initialization callback to add exception handler
    Statement initCallback =
        Stmt.newObject(parameterizedAs(InitializationCallback.class, typeParametersOf(ctx.getEnclosingType())))
            .extend()
            .publicOverridesMethod("init", Parameter.of(ctx.getEnclosingType(), "obj", true))
            .append(generateExceptionHandler(ctx))
            .append(Stmt.loadVariable(holderVar).invoke("set", Refs.get("handler")))
            .append(Stmt.castTo(ClientMessageBusImpl.class, Stmt.invokeStatic(ErraiBus.class, "get"))
                .invoke("addUncaughtExceptionHandler", Refs.get("handler"))).finish().finish();

    ctx.getTargetInjector().addStatementToEndOfInjector(
        Stmt.loadVariable("context").invoke("addInitializationCallback",
                  Refs.get(ctx.getInjector().getInstanceVarName()), initCallback));

    // Generate destruction callback to remove exception handler
    ctx.getTargetInjector().addStatementToEndOfInjector(
        Stmt.loadVariable("context").invoke(
            "addDestructionCallback",
                  Refs.get(ctx.getInjector().getInstanceVarName()),
                  InjectUtil.createDestructionCallback(ctx.getEnclosingType(), "obj",
                      Collections.singletonList(
                          (Statement) Stmt.castTo(ClientMessageBusImpl.class, Stmt.invokeStatic(ErraiBus.class, "get"))
                              .invoke("removeUncaughtExceptionHandler", Stmt.loadVariable(holderVar).invoke("get")))
                      )));

    return Collections.emptyList();
  }

  private Statement generateExceptionHandler(InjectableInstance<UncaughtException> ctx) {
    final Statement handlerStatement =
        Stmt.declareFinalVariable("handler", UncaughtExceptionHandler.class,
            Stmt.newObject(UncaughtExceptionHandler.class).extend()
                .publicMethod(void.class, "onUncaughtException", Parameter.of(Throwable.class, "t"))
                .append(ctx.callOrBind(Refs.get("t")))
                .finish()
                .finish());

    return handlerStatement;
  }
}