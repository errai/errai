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

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.*;
import org.mvel2.util.StringAppender;

import java.lang.annotation.Annotation;

public class InjectionTask {
  protected final TaskType injectType;
  protected final Injector injector;

  protected MetaConstructor constructor;
  protected MetaField field;
  protected MetaMethod method;
  protected MetaClass type;
  protected MetaParameter parm;


  public InjectionTask(Injector injector, MetaField field) {
    this.injectType = field.isPrivate() ? TaskType.PrivateField : TaskType.Field;
    this.injector = injector;
    this.field = field;
  }

  public InjectionTask(Injector injector, MetaMethod method) {
    this.injectType = TaskType.Method;
    this.injector = injector;
    this.method = method;
  }

  public InjectionTask(Injector injector, MetaParameter parm) {
    this.injectType = TaskType.Parameter;
    this.injector = injector;
    this.parm = parm;
  }

  public InjectionTask(Injector injector, MetaClass type) {
    this.injectType = TaskType.Type;
    this.injector = injector;
    this.type = type;
  }

  @SuppressWarnings({"unchecked"})
  public String doTask(InjectionContext ctx) {
    StringAppender appender = new StringAppender();
    InjectionPoint<? extends Annotation> injectionPoint
        = new InjectionPoint(null, injectType, constructor, method, field, type, parm, injector, ctx);

    Injector inj;

    switch (injectType) {
      case Type:
        ctx.getQualifiedInjector(type,
            JSR299QualifyingMetadata.createFromAnnotations(injectionPoint.getQualifiers()));
        break;

      case PrivateField:
        inj = ctx.getQualifiedInjector(field.getType(),
            JSR299QualifyingMetadata.createFromAnnotations(injectionPoint.getQualifiers()));

        appender.append(InjectUtil.getPrivateFieldInjectorName(field)).append("(")
            .append(injector.getVarName()).append(", ")
            .append(inj.getType(ctx, injectionPoint))
            .append(");\n");

        ctx.getPrivateFieldsToExpose().add(field);
        break;

      case Field:
        inj = ctx.getQualifiedInjector(field.getType(),
            JSR299QualifyingMetadata.createFromAnnotations(injectionPoint.getQualifiers()));

        appender.append(injector.getVarName()).append('.').append(field.getName()).append(" = ")
            .append(inj.getType(ctx, injectionPoint))
            .append(";\n");
        break;

      case Method:
        appender.append(injector.getVarName()).append('.')
            .append(method.getName()).append('(');

        String[] vars = InjectUtil.resolveInjectionDependencies(method.getParameters(), ctx, injectionPoint);

        appender.append(InjectUtil.commaDelimitedList(vars)).append(");\n");
        break;
    }

    return appender.toString();
  }

  public TaskType getInjectType() {
    return injectType;
  }

  public Injector getInjector() {
    return injector;
  }

  public MetaField getField() {
    return field;
  }

  public MetaMethod getMethod() {
    return method;
  }

  public void setMethod(MetaMethod method) {
    if (this.method == null)
      this.method = method;
  }

  public void setField(MetaField field) {
    if (this.field == null)
      this.field = field;
  }
}
