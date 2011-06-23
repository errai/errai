/*
 * Copyright 2011 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack;

import org.jboss.errai.ioc.rebind.ioc.codegen.AbstractStatement;
import org.jboss.errai.ioc.rebind.ioc.codegen.CallParameters;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.VariableReference;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.InvalidTypeException;
import org.jboss.errai.ioc.rebind.ioc.codegen.util.GenUtil;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ThrowException extends AbstractCallElement {
  private String exceptionVariableName;
  
  private Class<? extends Throwable> throwableType;
  private Object[] parameters;

  public ThrowException(String exceptionVariableName) {
    this.exceptionVariableName = exceptionVariableName;
    this.parameters = new Object[0];
  }

  public ThrowException(Class<? extends Throwable> throwableType, Object... parameters) {
    this.throwableType = throwableType;
    this.parameters = parameters;
  }

  public void handleCall(CallWriter writer, Context context, Statement statement) {
    statement = new AbstractStatement() {
      public String generate(Context context) {
        StringBuilder buf = new StringBuilder();
        buf.append("throw ");
        if (throwableType != null) {
          CallParameters parms = CallParameters.fromStatements(GenUtil.generateCallParameters(context, parameters));
          buf.append(ObjectBuilder.newInstanceOf(throwableType).withParameters(parms).generate(context));
        }
        else {
          VariableReference exceptionVar = context.getVariable(exceptionVariableName);
          if (!exceptionVar.getType().isAssignableTo(Throwable.class)) {
            throw new InvalidTypeException("Variable " + exceptionVariableName + " is not a Throwable");
          }
          buf.append(exceptionVar.generate(context));
        }
        return buf.toString();
      }
    };
    writer.reset();
    nextOrReturn(writer, context, statement);
  }
}