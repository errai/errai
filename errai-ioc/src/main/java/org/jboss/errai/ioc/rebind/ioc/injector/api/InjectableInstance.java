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

package org.jboss.errai.ioc.rebind.ioc.injector.api;


import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.literal.LiteralFactory;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaConstructor;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.ioc.rebind.ioc.injector.AsyncInjectUtil;
import org.jboss.errai.ioc.rebind.ioc.injector.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.injector.Injector;

import java.lang.annotation.Annotation;

public class InjectableInstance<T extends Annotation> extends InjectionPoint<T> {

  public InjectableInstance(final T annotation, final TaskType taskType, final MetaConstructor constructor, final MetaMethod method,
                            final MetaField field, final MetaClass type, final MetaParameter parm, final Injector injector, final InjectionContext injectionContext) {

    super(annotation, taskType, constructor, method, field, type, parm, injector, injectionContext);
  }

  public static <T extends Annotation> InjectableInstance<T> getInjectedInstance(final T annotation,
                                                                                 final MetaClass type,
                                                                                 final Injector injector,
                                                                                 final InjectionContext context) {
    return new InjectableInstance<T>(annotation, TaskType.Type, null, null, null, type,
        null, injector, context);

  }

  public static <T extends Annotation> InjectableInstance<T> getMethodInjectedInstance(final MetaMethod method,
                                                                                       final Injector injector,
                                                                                       final InjectionContext context) {

    //noinspection unchecked
    return new InjectableInstance(
        context.getMatchingAnnotationForElementType(WiringElementType.InjectionPoint, method),
        !method.isPublic() ? TaskType.PrivateMethod : TaskType.Method, null,
        method, null,
        method.getDeclaringClass(),
        null, injector, context);

  }

  public static <T extends Annotation> InjectableInstance<T> getParameterInjectedInstance(final MetaParameter parm,
                                                                                          final Injector injector,
                                                                                          final InjectionContext context) {

    if (parm.getDeclaringMember() instanceof MetaConstructor) {

      //noinspection unchecked
      return new InjectableInstance(context.getMatchingAnnotationForElementType(WiringElementType.InjectionPoint,
          parm.getDeclaringMember()),
          TaskType.Parameter, ((MetaConstructor) parm.getDeclaringMember()),
          null, null, parm.getDeclaringMember().getDeclaringClass(), parm, injector, context);
    }
    else {
      //noinspection unchecked
      return new InjectableInstance(context.getMatchingAnnotationForElementType(WiringElementType.InjectionPoint,
          parm.getDeclaringMember()),
          TaskType.Parameter, null,
          ((MetaMethod) parm.getDeclaringMember()), null, parm.getDeclaringMember().getDeclaringClass(),
          parm, injector, context);
    }
  }

  public static <T extends Annotation> InjectableInstance<T> getFieldInjectedInstance(final MetaField field,
                                                                                      final Injector injector,
                                                                                      final InjectionContext context) {

    //noinspection unchecked
    return new InjectableInstance(context.getMatchingAnnotationForElementType(WiringElementType.InjectionPoint,
        field),
        !field.isPublic() ? TaskType.PrivateField : TaskType.Field, null,
        null, field,
        field.getDeclaringClass(),
        null, injector, context);

  }

  /**
   * Returns an instance of a {@link Statement} which represents the value associated for injection at this
   * InjectionPoint.
   *
   * @return a statement representing the value of the injection point.
   */
  public Statement getValueStatement() {

    final Statement[] stmt;
    final Statement val;

    if (getTargetInjector().getInjectedType().equals(getEnclosingType()) &&
        getTargetInjector().getQualifyingMetadata().equals(getQualifyingMetadata())) {

      val = Refs.get(getInjector().getInstanceVarName());
    }
    else {
      val = Refs.get(getTargetInjector().getInstanceVarName());
    }

    switch (taskType) {
      case Field:
      case PrivateField:
        return InjectUtil.getPublicOrPrivateFieldValue(injectionContext,
            val,
            field);

      case PrivateMethod:
      case Method:
        if (method.getReturnType().isVoid()) {
          return Stmt.load(Void.class);
        }

        if (injectionContext.isAsync()) {
          stmt = AsyncInjectUtil.resolveInjectionDependencies(method.getParameters(), injectionContext, method);
        }
        else {
          stmt = InjectUtil.resolveInjectionDependencies(method.getParameters(), injectionContext, method);
        }


        return InjectUtil.invokePublicOrPrivateMethod(injectionContext,
            val,
            method,
            stmt);

      case Parameter:
        final Statement inlineStmt = injectionContext.getInlineBeanReference(parm);
        if (inlineStmt == null) {
          return Stmt.loadVariable("context").invoke("getBeanInstance",
              parm.getType(),
              InjectUtil.getQualifiersFromAnnotationsAsArray(parm.getAnnotations()));
        }
        else {
          return inlineStmt;
        }
      case Type:
        return val;

      default:
        return LiteralFactory.getLiteral(null);
    }
  }

  public Injector getTargetInjector() {
    final MetaClass targetType = getInjector() == null ? getEnclosingType() : getInjector().getInjectedType();

    Injector targetInjector
        = isProxy() ? injectionContext.getProxiedInjector(targetType, getQualifyingMetadata()) :
        injectionContext.getQualifiedInjector(targetType, getQualifyingMetadata());

    if (!isProxy()) {
      if (!targetInjector.isCreated()) {
        targetInjector = InjectUtil.getOrCreateProxy(injectionContext, getEnclosingType(), getQualifyingMetadata());
        if (targetInjector.isEnabled()) {
          targetInjector.getBeanInstance(this);
        }
      }
    }

    return targetInjector;
  }

  public Statement callOrBind(final Statement... values) {
    final Injector targetInjector = injector;

    MetaMethod meth = method;
    switch (taskType) {
      case PrivateField:
      case Field:
        return InjectUtil.setPublicOrPrivateFieldValue(
            injectionContext,
            Refs.get(targetInjector.getInstanceVarName()),
            field,
            values[0]);

      case Parameter:
        if (parm.getDeclaringMember() instanceof MetaMethod) {
          meth = (MetaMethod) parm.getDeclaringMember();
        }
        else {
          throw new RuntimeException("cannot call task on element: " + parm.getDeclaringMember());
        }

      case Method:
      case PrivateMethod:
        return InjectUtil.invokePublicOrPrivateMethod(injectionContext,
            Refs.get(targetInjector.getInstanceVarName()),
            meth,
            values);

      case Type:
      default:
        throw new RuntimeException("cannot call tasktype: " + taskType);
    }
  }
}
