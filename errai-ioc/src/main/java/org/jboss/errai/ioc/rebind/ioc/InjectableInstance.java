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

package org.jboss.errai.ioc.rebind.ioc;

import static org.jboss.errai.ioc.rebind.ioc.InjectUtil.getPrivateFieldInjectorName;
import static org.jboss.errai.ioc.rebind.ioc.InjectUtil.getPrivateMethodName;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.literal.LiteralFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaConstructor;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaField;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaParameter;
import org.jboss.errai.ioc.rebind.ioc.codegen.util.Refs;
import org.jboss.errai.ioc.rebind.ioc.codegen.util.Stmt;

public class InjectableInstance<T extends Annotation> {
  private T annotation;
  private TaskType taskType;

  private MetaConstructor constructor;
  private MetaMethod method;
  private MetaField field;
  private MetaClass type;
  private MetaParameter parm;
  private Injector injector;
  private InjectionContext injectionContext;

  public InjectableInstance(T annotation, TaskType taskType, MetaConstructor constructor, MetaMethod method,
                            MetaField field, MetaClass type, MetaParameter parm, Injector injector, InjectionContext injectionContext) {
    this.annotation = annotation;
    this.taskType = taskType;
    this.constructor = constructor;
    this.method = method;
    this.field = field;
    this.type = type;
    this.parm = parm;
    this.injector = injector;
    this.injectionContext = injectionContext;
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

  public T getAnnotation() {
    return annotation;
  }

  public MetaConstructor getConstructor() {
    return constructor;
  }

  public TaskType getTaskType() {
    return taskType;
  }

  public MetaMethod getMethod() {
    return method;
  }

  public MetaField getField() {
    return field;
  }

  public MetaClass getType() {
    return type;
  }

  public MetaParameter getParm() {
    return parm;
  }

  public Injector getInjector() {
    return injector;
  }

  public InjectionContext getInjectionContext() {
    return injectionContext;
  }

  public void ensureMemberExposed() {
    switch (taskType) {
      case PrivateMethod:
        injectionContext.addExposedMethod(method);
        break;
      case PrivateField:
        injectionContext.addExposedField(field);
        break;
    }
  }

  /**
   * Returns an instance of a {@link Statement} which represents the value associated for injection at this
   * InjectionPoint.
   *
   * @return
   */
  public Statement getValueStatement() {
    Statement[] stmt;
    switch (taskType) {
      case PrivateField:
        return Stmt.invokeStatic(injectionContext.getProcessingContext().getBootstrapClass(),
                getPrivateFieldInjectorName(field), Refs.get(injector.getVarName()));

      case Field:
        return Stmt.loadVariable(injector.getVarName()).loadField(field.getName());

      case PrivateMethod:
        if (method.getReturnType().isVoid()) {
          return Stmt.load(Void.class);
        }

        MetaParameter[] methParms = method.getParameters();
        Statement[] resolveParmsDeps = InjectUtil.resolveInjectionDependencies(methParms, injectionContext, method);
        stmt = new Statement[methParms.length + 1];
        stmt[0] = Refs.get(injector.getVarName());
        System.arraycopy(resolveParmsDeps, 0, stmt, 1, methParms.length);

        //todo: this
        return Stmt.invokeStatic(injectionContext.getProcessingContext().getBootstrapClass(),
                getPrivateMethodName(method), stmt);

      case Method:
        stmt = InjectUtil.resolveInjectionDependencies(method.getParameters(), injectionContext, method);

        return Stmt.loadVariable(injector.getVarName()).invoke(method, stmt);

      case StaticMethod:
        stmt = InjectUtil.resolveInjectionDependencies(method.getParameters(), injectionContext, method);

        return Stmt.invokeStatic(method.getDeclaringClass(), method.getName(), stmt);

      case Parameter:
      case Type:
        return Refs.get(injector.getVarName());

      default:
        return LiteralFactory.getLiteral(null);
    }
  }

  public String getMemberName() {
    switch (taskType) {
      case PrivateField:
        return getPrivateFieldInjectorName(field) + "(" + injector.getVarName() + ")";

      case Field:
        return field.getName();

      case Parameter:
      case StaticMethod:
      case PrivateMethod:
      case Method:
        return method.getName();

      case Type:
        return type.getName();

      default:
        return null;
    }
  }

  public Annotation[] getQualifiers() {
    List<Annotation> annotations;
    switch (taskType) {
      case PrivateField:
      case Field:
        annotations = InjectUtil.extractQualifiersFromField(field);
        return annotations.toArray(new Annotation[annotations.size()]);

      case Parameter:
        annotations = InjectUtil.extractQualifiersFromParameter(parm);
        return annotations.toArray(new Annotation[annotations.size()]);

      case PrivateMethod:
      case Method:
        annotations = InjectUtil.extractQualifiersFromMethod(method);
        return annotations.toArray(new Annotation[annotations.size()]);

      case Type:
        annotations = InjectUtil.extractQualifiersFromType(type);
        return annotations.toArray(new Annotation[annotations.size()]);

      default:
        return new Annotation[0];
    }
  }

  public Annotation[] getAnnotations(Field field) {
    return field == null ? null : field.getAnnotations();
  }
}
