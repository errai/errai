/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

import java.util.Arrays;

import org.jboss.errai.codegen.framework.CallParameters;
import org.jboss.errai.codegen.framework.Context;
import org.jboss.errai.codegen.framework.MethodInvocation;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.exception.InvalidExpressionException;
import org.jboss.errai.codegen.framework.exception.InvalidTypeException;
import org.jboss.errai.codegen.framework.exception.OutOfScopeException;
import org.jboss.errai.codegen.framework.exception.TypeNotIterableException;
import org.jboss.errai.codegen.framework.exception.UndefinedMethodException;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.meta.MetaMethod;
import org.jboss.errai.codegen.framework.meta.MetaParameterizedType;
import org.jboss.errai.codegen.framework.meta.MetaType;
import org.jboss.errai.codegen.framework.meta.MetaTypeVariable;
import org.jboss.errai.codegen.framework.util.GenUtil;

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
        callType.getBestMatchingMethod(methodName, parameterTypes);
           throw new UndefinedMethodException(statement.getType(), methodName, parameterTypes);
//        dummyReturn(writer, context);
//        return;
      }

      if (method.getGenericParameterTypes() != null) {
        MetaType[] genTypes = method.getGenericParameterTypes();
        for (int i = 0; i < genTypes.length; i++) {
           if (genTypes[i] instanceof MetaParameterizedType) {
             if (parameters[i] instanceof MetaClass) {
               MetaType type = ((MetaParameterizedType) genTypes[i]).getTypeParameters()[0];
               if (type instanceof MetaTypeVariable) {
                 writer.recordTypeParm(((MetaTypeVariable) type).getName(), (MetaClass) parameters[i]);
               }
             }
           }
        }
        
      }
      
      /**
       * If the method is within the calling scope, we can strip the qualifying reference.
       */
      if (statement instanceof LoadClassReference.ClassReference && context.isInScope(method)) {
        writer.reset();
      }

      callParams = fromStatements(GenUtil.generateCallParameters(method, context, parameters));
      statement = new MethodInvocation(writer, callType, method, callParams);

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

//      nextOrReturn(writer, context, new Statement() {
//        @Override
//        public String generate(Context context) {
//          return methodName + "(" + Arrays.toString(parameters) + ")";
//        }
//
//        @Override
//        public MetaClass getType() {
//          return MetaClassFactory.get(Object.class);
//        }
//      });
    }
  }

  @Override
  public String toString() {
    return "[[MethodCall<" + methodName + "(" + Arrays.toString(parameters) + ")>]" + next + "]";
  }

  private void dummyReturn(CallWriter writer, Context context) {
    nextOrReturn(writer, context, new Statement() {
      @Override
      public String generate(Context context) {
        return methodName + "(" + Arrays.toString(parameters) + ")";
      }

      @Override
      public MetaClass getType() {
        return MetaClassFactory.get(Object.class);
      }
    });
  }
}