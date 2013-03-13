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

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaConstructor;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.util.PrivateAccessType;
import org.jboss.errai.ioc.rebind.ioc.injector.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.injector.Injector;
import org.jboss.errai.ioc.rebind.ioc.metadata.QualifyingMetadata;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

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

  public InjectionPoint(final T annotation,
                        final TaskType taskType,
                        final MetaConstructor constructor,
                        final MetaMethod method,
                        final MetaField field,
                        final MetaClass type,
                        final MetaParameter parm,
                        final Injector injector,
                        final InjectionContext injectionContext) {

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

  public Annotation getRawAnnotation() {
    return (Annotation) annotation;
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

  /**
   * Returns the element type or a method return type, based on what the injection point is.
   * <p/>
   * <strong>Parameters</strong>:
   * <pre><code>
   *  public class MyClass {
   *   public void MyClass(@A Set set) {
   *   }
   * <p/>
   *   public void setMethod(Foo foo, @B Bar t) {
   *   }
   *  }
   * </code></pre>
   * If the element being decorated is the parameter where <tt>@A</tt> represents the injection/decorator point,
   * then the type returned by this method will be <tt>Set</tt>. If the element being decorated is the parameter
   * where <tt>@B</tt> represents the injection point, then the type returned by this method will be <tt>Bar</tt>.
   * <p/>
   * <strong>Fields</strong>:
   * <pre><code>
   *  public class MyClass {
   *    {@literal @}A private Map myField;
   *  }
   * </code></pre>
   * If the element being decorated is the field where <tt>@A</tt> represents the injection/decorator point,
   * then the type returned by this method wil be <tt>Map</tt>.
   * <p/>
   * <strong>Methods</strong>:
   * <pre><code>
   *  public class MyClass {
   *    {@literal @}A private List getList() {
   *    }
   * <p/>
   *    {@literal @}B private void doSomething() {
   *    }
   *  }
   * </code></pre>
   * If the element being decorated is the method where <tt>@A</tt> represents the injection/decorator point,
   * then the type returned by this method will be <tt>List</tt>. If the element being decorated is the method
   * where <tt>@B</tt> represents the injection/decorator point, then the type returned by this method will be
   * <tt>void</tt>.
   * <p/>
   * <strong>Constructor and Types</strong>:
   * <pre><code>
   *  {@literal @}A
   *  public class MyClass {
   *    {@literal @}B
   *    public MyClass() {
   *    }
   *  }
   * </code></pre>
   * If the class element being decorated is the method where <tt>@A</tt> represents the injection/decorator point,
   * then the type returned by this method will be <tt>MyClass</tt>. Also, if the constructor element being
   * decorated is the constructor where <tt>@B</tt> represents the injection/decorator point, then the type
   * returned by this method will be <tt>MyClass</tt>.
   *
   * @return The underlying type of the element or return type for a method.
   */
  public MetaClass getElementTypeOrMethodReturnType() {
    switch (taskType) {
      case PrivateField:
      case Field:
        return field.getType();
      case PrivateMethod:
      case Method:
        return method.getReturnType();
      case Parameter:
        return parm.getType();
      case Type:
        return type;
      default:
        throw new RuntimeException("unsupported operation: getType for task: " + taskType);
    }
  }

  public MetaClass getElementType() {
    switch (taskType) {
      case PrivateField:
      case Field:
        return field.getType();
      case PrivateMethod:
      case Method:
      case Parameter:
        return parm.getType();
      case Type:
        return type;
      default:
        throw new RuntimeException("unsupported operation: getType for task: " + taskType);
    }
  }

  /**
   * Returns the parameter reference if the injection point is a parameter, otherwise returns <tt>null</tt>.
   *
   * @return the {@link MetaParameter} reference if the injection point is a parameter, otherwise <tt>null</tt>.
   */
  public MetaParameter getParm() {
    return parm;
  }

  /**
   * Returns the {@link Injector} reference for the the bean
   *
   * @return
   */
  public Injector getInjector() {
    return injector;
  }

  public InjectionContext getInjectionContext() {
    return injectionContext;
  }

  public void ensureMemberExposed() {
    ensureMemberExposed(PrivateAccessType.Both);
  }

  public void ensureMemberExposed(final PrivateAccessType accessType) {
    switch (taskType) {
      case Parameter:
        if (parm.getDeclaringMember() instanceof MetaMethod) {
          final MetaMethod declMeth = (MetaMethod) parm.getDeclaringMember();
          injectionContext.addExposedMethod(declMeth);
        }
        break;
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
      case Field:
        return field.getName();

      case Parameter:
        return parm.getName();
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
        return parm.getDeclaringMember().getDeclaringClass();
      default:
        throw new RuntimeException("unsupported operation: getEncodingType for task: " + taskType);
    }
  }

  public Annotation[] getQualifiers() {
    final List<Annotation> annotations;
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

  public boolean isProxy() {
    return injectionContext.isProxiedInjectorRegistered(getEnclosingType(), getQualifyingMetadata());
  }

  public boolean isAnnotationPresent(Class<? extends Annotation> annotation) {
    return getAnnotation(annotation) != null;
  }

  public <A extends Annotation> A getAnnotation(Class<A> annotation) {
    for (Annotation a : Arrays.asList(getAnnotations())) {
      if (annotation != null && annotation.isAssignableFrom(a.annotationType())) {
        return (A) a;
      }
    }
    return null;
  }

  public Annotation[] getAnnotations() {
    switch (taskType) {
      case PrivateField:
      case Field:
        return field.getAnnotations();

      case Parameter:
        return parm.getAnnotations();

      case PrivateMethod:
      case Method:
        return method.getAnnotations();

      case Type:
        return type.getAnnotations();

      default:
        return new Annotation[0];
    }
  }
}
