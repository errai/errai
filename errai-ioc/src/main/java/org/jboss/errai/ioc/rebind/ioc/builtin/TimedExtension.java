/*
 * Copyright 2012 JBoss, by Red Hat, Inc
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

package org.jboss.errai.ioc.rebind.ioc.builtin;

import com.google.gwt.user.client.Timer;
import org.jboss.errai.codegen.ArithmeticOperator;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.util.Arith;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.client.api.Timed;
import org.jboss.errai.ioc.client.container.DestructionCallback;
import org.jboss.errai.ioc.client.container.InitializationCallback;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.injector.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Mike Brock
 */
@CodeDecorator
public class TimedExtension extends IOCDecoratorExtension<Timed> {
  public TimedExtension(Class<Timed> decoratesWith) {
    super(decoratesWith);
  }

  @Override
  public List<? extends Statement> generateDecorator(InjectableInstance<Timed> ctx) {
    try {
      final MetaClass beanClass = ctx.getEnclosingType();
      final Timed timed = ctx.getAnnotation();


      final Statement methodInvokation
          = InjectUtil.invokePublicOrPrivateMethod(ctx.getInjectionContext(), Refs.get("beanInstance"),
          ctx.getMethod());

      final org.jboss.errai.common.client.util.TimeUnit timeUnit = timed.timeUnit();
      final int interval = timed.interval();

      final Statement timerDecl
          = Stmt.nestedCall(Stmt.newObject(Timer.class).extend()
          .publicOverridesMethod("run")
          .append(methodInvokation)
          .finish().finish());

      final String timerVarName = InjectUtil.getUniqueVarName();
      final Statement timerVar = Stmt.declareFinalVariable(timerVarName, Timer.class, timerDecl);

      final List<Statement> statements = new ArrayList<Statement>();

      final Statement timerExec;
      switch (timed.type()) {
        case REPEATING:
          timerExec = Stmt.loadVariable(timerVarName).invoke("scheduleRepeating", timeUnit.toMillis(interval));
          break;
        default:
        case DELAYED:
          timerExec = Stmt.loadVariable(timerVarName).invoke("schedule", timeUnit.toMillis(interval));
          break;
      }

      final Statement destructionCallbackStmt
          = InjectUtil.createDestructionCallback(beanClass, "beanInstance",
              Collections.<Statement>singletonList(Stmt.loadVariable(timerVarName).invoke("cancel")));

      final Statement initCallbackStmt = InjectUtil.createInitializationCallback(beanClass, "beanInstance",
          Arrays.asList(timerVar,
              Stmt.loadVariable("context").invoke("addDestructionCallback",
                  Refs.get(ctx.getInjector().getInstanceVarName()), destructionCallbackStmt),
              timerExec));

      statements.add(Stmt.loadVariable("context").invoke("addInitializationCallback",
          Refs.get(ctx.getInjector().getInstanceVarName()), initCallbackStmt));

      return statements;
    }

    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
