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

package org.jboss.errai.codegen.framework.builder.callstack;

import static org.jboss.errai.codegen.framework.CallParameters.fromStatements;

import org.jboss.errai.codegen.framework.CallParameters;
import org.jboss.errai.codegen.framework.Context;
import org.jboss.errai.codegen.framework.MethodInvocation;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.exception.*;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaMethod;
import org.jboss.errai.codegen.framework.util.GenUtil;

import java.util.Arrays;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class MethodCall extends AbstractCallElement {
  private String methodName;
  private Object[] parameters;
  private boolean staticMethod;

  public MethodCall(String methodName, Object[] parameters) {
    this.methodName = methodName;
    this.parameters = parameters;
  }

  public MethodCall(String methodName, Object[] parameters, boolean staticMethod) {
    this.methodName = methodName;
    this.parameters = parameters;
    this.staticMethod = staticMethod;
  }

  @Override
  public void handleCall(CallWriter writer, Context context, Statement statement) {
    try {
      CallParameters callParams = fromStatements(GenUtil.generateCallParameters(context, parameters));

      statement.generate(context);
      
      MetaClass callType = statement.getType();

      MetaClass[] parameterTypes = callParams.getParameterTypes();
      final MetaMethod method = (staticMethod) ? callType.getBestMatchingStaticMethod(methodName, parameterTypes)
              : callType.getBestMatchingMethod(methodName, parameterTypes);

      if (method == null) {
        throw new UndefinedMethodException(statement.getType(), methodName, parameterTypes);
      }

      /**
       * If the method is within the calling scope, we can strip the qualifying reference.
       */
      if (statement instanceof LoadClassReference.ClassReference && context.isInScope(method)) {
        writer.reset();
      }

      callParams = fromStatements(GenUtil.generateCallParameters(method, context, parameters));
      statement = new MethodInvocation(method, callParams);
      
      resultType = statement.getType();

      nextOrReturn(writer, context, statement);
    }
    catch (OutOfScopeException e) {
     throw e;
    }
    catch (InvalidExpressionException e) {
      throw e;
    }
    catch (InvalidTypeException e) {
      throw e;
    }
    catch (UndefinedMethodException e) {
      throw e;
    }
    catch (TypeNotIterableException e) {
      throw e;
    }
    catch (Exception e) {
      throw new RuntimeException("error generating method call for: " + methodName
              + "(" + Arrays.toString(parameters) + ")", e);
    }
  }

  @Override
  public String toString() {
    return "[[MethodCall<" + methodName + "(" + Arrays.toString(parameters) + ")>]" + next + "]";
  }
}