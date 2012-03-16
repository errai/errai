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

package org.jboss.errai.ioc.rebind.ioc;

import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.literal.LiteralFactory;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaConstructor;
import org.jboss.errai.codegen.framework.meta.MetaField;
import org.jboss.errai.codegen.framework.meta.MetaMethod;
import org.jboss.errai.codegen.framework.meta.MetaParameter;
import org.jboss.errai.codegen.framework.util.Refs;
import org.jboss.errai.codegen.framework.util.Stmt;
import org.jboss.errai.ioc.rebind.ioc.exception.NonFatalFailedDependency;

import java.lang.annotation.Annotation;

import static org.jboss.errai.codegen.framework.util.GenUtil.getPrivateFieldInjectorName;
import static org.jboss.errai.codegen.framework.util.GenUtil.getPrivateMethodName;

public class InjectableInstance<T extends Annotation> extends InjectionPoint<T> {

  public InjectableInstance(T annotation, TaskType taskType, MetaConstructor constructor, MetaMethod method,
                            MetaField field, MetaClass type, MetaParameter parm, Injector injector, InjectionContext injectionContext) {

    super(annotation, taskType, constructor, method, field, type, parm, injector, injectionContext);
  }

  public static <T extends Annotation> InjectableInstance<T> getTypeInjectedInstance(T annotation,
                                                                                     MetaClass type,
                                                                                     Injector injector,
                                                                                     InjectionContext context) {
    return new InjectableInstance<T>(annotation, TaskType.Type, null, null, null, type,
            null, injector, context);

  }

  public static <T extends Annotation> InjectableInstance<T> getMethodInjectedInstance(T annotation,
                                                                                       MetaMethod method,
                                                                                       Injector injector,
                                                                                       InjectionContext context) {

    return new InjectableInstance<T>(annotation, !method.isPublic() ? TaskType.PrivateMethod : TaskType.Method, null,
            method, null,
            method.getDeclaringClass(),
            null, injector, context);

  }

  public static <T extends Annotation> InjectableInstance<T> getStaticMethodInjectedInstance(T annotation,
                                                                                             MetaMethod method,
                                                                                             Injector injector,
                                                                                             InjectionContext
                                                                                                     context) {
    return new InjectableInstance<T>(annotation, TaskType.StaticMethod,
            null, method,
            null, null, null,
            injector, context);
  }


  public static <T extends Annotation> InjectableInstance<T> getFieldInjectedInstance(T annotation,
                                                                                      MetaField field,
                                                                                      Injector injector,
                                                                                      InjectionContext context) {

    return new InjectableInstance<T>(annotation, !field.isPublic() ? TaskType.PrivateField : TaskType.Field, null,
            null, field,
            field.getDeclaringClass(),
            null, injector, context);

  }

  /**
   * Returns an instance of a {@link Statement} which represents the value associated for injection at this
   * InjectionPoint.
   *
   * @return
   */
  public Statement getValueStatement() {
    final Injector targetInjector
            = isProxy() ? injectionContext.getProxiedInjector(getEnclosingType(), getQualifyingMetadata()) :
            injectionContext.getQualifiedInjector(getEnclosingType(), getQualifyingMetadata());

    Statement[] stmt;
    switch (taskType) {
      case PrivateField:
        return Stmt.invokeStatic(injectionContext.getProcessingContext().getBootstrapClass(),
                getPrivateFieldInjectorName(field), Refs.get(targetInjector.getVarName()));

      case Field:
        return Stmt.loadVariable(targetInjector.getVarName()).loadField(field.getName());

      case PrivateMethod:
        if (method.getReturnType().isVoid()) {
          return Stmt.load(Void.class);
        }

        MetaParameter[] methParms = method.getParameters();
        Statement[] resolveParmsDeps = InjectUtil.resolveInjectionDependencies(methParms, injectionContext, method);
        stmt = new Statement[methParms.length + 1];
        stmt[0] = Refs.get(targetInjector.getVarName());
        System.arraycopy(resolveParmsDeps, 0, stmt, 1, methParms.length);

        //todo: this
        return Stmt.invokeStatic(injectionContext.getProcessingContext().getBootstrapClass(),
                getPrivateMethodName(method), stmt);

      case Method:
        stmt = InjectUtil.resolveInjectionDependencies(method.getParameters(), injectionContext, method);

        return Stmt.loadVariable(targetInjector.getVarName()).invoke(method, stmt);

      case StaticMethod:
        stmt = InjectUtil.resolveInjectionDependencies(method.getParameters(), injectionContext, method);

        return Stmt.invokeStatic(method.getDeclaringClass(), method.getName(), stmt);

      case Parameter:
      case Type:
        return Refs.get(targetInjector.getVarName());

      default:
        return LiteralFactory.getLiteral(null);
    }
  }

  public Statement callOrBind(Statement... values) {
    final Injector targetInjector = injector;

    MetaMethod meth = method;
    switch (taskType) {
      case PrivateField:
        Statement[] args = new Statement[values.length + 1];
        args[0] = Refs.get(targetInjector.getVarName());
        System.arraycopy(values, 0, args, 1, values.length);

        return Stmt.invokeStatic(injectionContext.getProcessingContext().getBootstrapClass(),
                getPrivateFieldInjectorName(field), args);

      case Field:
        return Stmt.loadVariable(targetInjector.getVarName()).loadField(field.getName()).assignValue(values[0]);

      case Parameter:
        if (parm.getDeclaringMember() instanceof MetaMethod) {
          meth = (MetaMethod) parm.getDeclaringMember();
        }
        else {
          throw new RuntimeException("cannot call task on element: " + parm.getDeclaringMember());
        }

      case Method:
      case StaticMethod:
      case PrivateMethod:
        args = new Statement[values.length + 1];
        args[0] = Refs.get(targetInjector.getVarName());
        System.arraycopy(values, 0, args, 1, values.length);

        if (!meth.isPublic()) {
          return Stmt.invokeStatic(injectionContext.getProcessingContext().getBootstrapClass(),
                  getPrivateMethodName(meth), args);
        }
        else {
          return Stmt.loadVariable(targetInjector.getVarName()).invoke(meth, values);
        }

      default:
        throw new RuntimeException("cannot call tasktype: " + taskType);
    }
  }
}
