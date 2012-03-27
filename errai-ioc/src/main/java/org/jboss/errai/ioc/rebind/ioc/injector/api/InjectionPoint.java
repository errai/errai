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

import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaConstructor;
import org.jboss.errai.codegen.framework.meta.MetaField;
import org.jboss.errai.codegen.framework.meta.MetaMethod;
import org.jboss.errai.codegen.framework.meta.MetaParameter;
import org.jboss.errai.codegen.framework.util.PrivateAccessType;
import org.jboss.errai.ioc.rebind.ioc.exception.InjectionFailure;
import org.jboss.errai.ioc.rebind.ioc.injector.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.injector.Injector;
import org.jboss.errai.ioc.rebind.ioc.metadata.QualifyingMetadata;

import java.lang.annotation.Annotation;
import java.util.List;

import static org.jboss.errai.codegen.framework.util.GenUtil.getPrivateFieldInjectorName;

/**
 * @author Mike Brock
 */
public class InjectionPoint<T> {
  protected T annotation;
  protected TaskType taskType;

  protected MetaConstructor constructor;
  protected MetaMethod method;
  protected MetaField field;
  protected MetaClass type;
  protected MetaParameter parm;
  protected Injector injector;
  protected InjectionContext injectionContext;

  public InjectionPoint(T annotation, TaskType taskType, MetaConstructor constructor, MetaMethod method,
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
    if (type != null) return type;

    switch (taskType) {
      case PrivateField:
      case Field:
        return type = field.getType();
      case PrivateMethod:
      case Parameter:
        return type = parm.getType();
      case Type:
        return type;
      default:
        throw new RuntimeException("unsupported operation: getType for task: " + taskType);
    }
  }

  public MetaClass getElementTypeOrMethodReturnType() {
    switch (taskType) {
      case PrivateField:
      case Field:
        return type = field.getType();
      case PrivateMethod:
      case Method:
        return type = method.getReturnType();
      case Parameter:
        return type = parm.getType();
      case Type:
        return type;
      default:
        throw new RuntimeException("unsupported operation: getType for task: " + taskType);
    }
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
    ensureMemberExposed(PrivateAccessType.Both);
  }

  public void ensureMemberExposed(PrivateAccessType accessType) {
    switch (taskType) {
      case Parameter:
        if (parm.getDeclaringMember() instanceof MetaMethod) {
          MetaMethod declMeth = (MetaMethod) parm.getDeclaringMember();
          injectionContext.addExposedMethod(declMeth);
        }
      case PrivateMethod:
        injectionContext.addExposedMethod(method);
        break;
      case PrivateField:
        injectionContext.addExposedField(field, accessType);
        break;
    }
  }

  public String getMemberName() {
    switch (taskType) {
      case PrivateField:
        return getPrivateFieldInjectorName(field) + "(" + injector.getVarName() + ")";

      case Field:
        return field.getName();

      case Parameter:
      case PrivateMethod:
      case Method:
        return method.getName();

      case Type:
        return type.getName();

      default:
        return null;
    }
  }

  public MetaClass getEnclosingType() {
    switch (taskType) {
      case PrivateField:
      case Field:
        return field.getDeclaringClass();
      case PrivateMethod:
      case Method:
        return method.getDeclaringClass();
      case Type:
        return type;
      case Parameter:
        return parm.getType();
      default:
        throw new RuntimeException("unsupported operation: getEncodingType for task: " + taskType);
    }
  }

  public Annotation[] getQualifiers() {
    List<Annotation> annotations;
    switch (taskType) {
      case PrivateField:
      case Field:
        annotations = InjectUtil.getQualifiersFromAnnotations(field.getAnnotations());
        return annotations.toArray(new Annotation[annotations.size()]);

      case Parameter:
        annotations = InjectUtil.getQualifiersFromAnnotations(parm.getAnnotations());
        return annotations.toArray(new Annotation[annotations.size()]);

      case PrivateMethod:
      case Method:
        annotations = InjectUtil.getQualifiersFromAnnotations(method.getAnnotations());
        return annotations.toArray(new Annotation[annotations.size()]);

      case Type:
        annotations = InjectUtil.getQualifiersFromAnnotations(type.getAnnotations());
        return annotations.toArray(new Annotation[annotations.size()]);

      default:
        return new Annotation[0];
    }
  }

  public QualifyingMetadata getQualifyingMetadata() {
    return injectionContext.getProcessingContext().getQualifyingMetadataFactory().createFrom(getQualifiers());
  }

  private Boolean _isProxyCache;

  public boolean isProxy() {
    if (_isProxyCache != null) return _isProxyCache;
    try {
      return _isProxyCache = injectionContext.isProxiedInjectorRegistered(getEnclosingType(), getQualifyingMetadata());
    }
    catch (InjectionFailure e) {
      return _isProxyCache = false;
    }
  }
}
