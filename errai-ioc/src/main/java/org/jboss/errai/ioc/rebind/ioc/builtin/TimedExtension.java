/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.ioc.rebind.ioc.builtin;

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.meta.MetaAnnotation;
import org.jboss.errai.codegen.meta.MetaEnum;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.common.client.util.TimeUnit;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.client.api.Timed;
import org.jboss.errai.ioc.client.api.TimerType;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.injector.api.Decorable;
import org.jboss.errai.ioc.rebind.ioc.injector.api.FactoryController;

import com.google.gwt.user.client.Timer;

import static org.jboss.errai.ioc.client.api.TimerType.REPEATING;

/**
 * @author Mike Brock
 */
@CodeDecorator
public class TimedExtension extends IOCDecoratorExtension<Timed> {
  public TimedExtension(Class<Timed> decoratesWith) {
    super(decoratesWith);
  }

  @Override
  public void generateDecorator(Decorable decorable, FactoryController controller) {
    try {
      final MetaAnnotation timed = decorable.getAnnotation();


      final Statement methodInvokation
          = decorable.getAccessStatement();

      final TimeUnit timeUnit = timed.<MetaEnum>value("timeUnit").as(TimeUnit.class);
      final int interval = timed.value("interval");

      final Statement timerDecl
          = Stmt.nestedCall(Stmt.newObject(Timer.class).extend()
          .publicOverridesMethod("run")
          .append(methodInvokation)
          .finish().finish());

      final String timerVarName = decorable.getAsMethod().getName() + "Timer";
      final Statement timerVar = controller.getReferenceStmt(timerVarName, Timer.class);

      final List<Statement> initStmts = new ArrayList<Statement>();
      final List<Statement> destructionStmts = new ArrayList<Statement>();

      initStmts.add(controller.setReferenceStmt(timerVarName, timerDecl));

      final Statement timerExec;
      final MetaEnum type = timed.value("type");

      switch (TimerType.valueOf(type.name())) {
        case REPEATING:
          timerExec = Stmt.nestedCall(timerVar).invoke("scheduleRepeating", timeUnit.toMillis(interval));
          break;
        default:
        case DELAYED:
          timerExec = Stmt.nestedCall(timerVar).invoke("schedule", timeUnit.toMillis(interval));
          break;
      }

      initStmts.add(timerExec);
      destructionStmts.add(Stmt.nestedCall(timerVar).invoke("cancel"));

      controller.addInitializationStatements(initStmts);
      controller.addDestructionStatements(destructionStmts);
    }

    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
