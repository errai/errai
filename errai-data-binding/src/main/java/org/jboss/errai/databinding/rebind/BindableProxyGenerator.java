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

package org.jboss.errai.databinding.rebind;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.jboss.errai.codegen.Cast;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Variable;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.util.Bool;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.databinding.client.BindableProxy;
import org.jboss.errai.databinding.client.api.Bindable;

import com.google.gwt.user.client.ui.HasValue;

/**
 * Generates the proxy for a {@link Bindable} type.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class BindableProxyGenerator {
  private Class<?> bindable;

  public BindableProxyGenerator(Class<?> bindable) {
    this.bindable = bindable;
  }

  public ClassStructureBuilder<?> generate() {
    ClassStructureBuilder<?> classBuilder = ClassBuilder.define(bindable.getSimpleName() + "Proxy", bindable)
        .packageScope()
        .implementsInterface(BindableProxy.class)
        .body()
        .privateField("hasValue", HasValue.class)
        .finish()
        .privateField("target", bindable)
        .finish()
        .publicConstructor()
        .finish()
        .publicConstructor(Parameter.of(HasValue.class, "hasValue"), Parameter.of(bindable, "target"))
        .append(Stmt.loadClassMember("hasValue").assignValue(Variable.get("hasValue")))
        .append(Stmt.loadClassMember("target").assignValue(Variable.get("target")))
        .finish();

    BeanInfo beanInfo;
    try {
      beanInfo = Introspector.getBeanInfo(bindable);
      generateProxyMethods(beanInfo.getPropertyDescriptors(), classBuilder);
    }
    catch (IntrospectionException e) {
      throw new RuntimeException("Failed to introspect bean:" + bindable.getName(), e);
    }

    return classBuilder;
  }

  private void generateProxyMethods(PropertyDescriptor[] propertyDescriptors, ClassStructureBuilder<?> classBuilder) {
    BlockBuilder<?> setMethod = classBuilder.publicMethod(void.class, "set",
        Parameter.of(String.class, "property"),
        Parameter.of(Object.class, "value"));

    if (propertyDescriptors != null) {
      for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {

        Method setterMethod = propertyDescriptor.getWriteMethod();
        if (setterMethod != null && !Modifier.isFinal(setterMethod.getModifiers())) {
          setMethod
              .append(Stmt
                  .if_(Bool.expr(Stmt.loadVariable("property").invoke("equals", propertyDescriptor.getName())))
                  .append(Stmt.loadVariable("target").invoke(
                      setterMethod.getName(), Cast.to(setterMethod.getParameterTypes()[0], Stmt.loadVariable("value"))))
                  .finish()
              );

          classBuilder.publicMethod(setterMethod.getReturnType(), setterMethod.getName(),
              Parameter.of(setterMethod.getParameterTypes()[0], propertyDescriptor.getName()))
              .append(Stmt
                    .loadClassMember("target").invoke(setterMethod.getName(),
                        Cast.to(setterMethod.getParameterTypes()[0], Stmt.loadVariable(propertyDescriptor.getName()))))
              .append(Stmt
                  .loadClassMember("hasValue").invoke("setValue",
                        Stmt.loadVariable(propertyDescriptor.getName()), true))
              .finish();
        }

        Method getterMethod = propertyDescriptor.getReadMethod();
        if (getterMethod != null && !Modifier.isFinal(getterMethod.getModifiers())) {
          classBuilder.publicMethod(getterMethod.getReturnType(), getterMethod.getName())
              .append(Stmt.loadClassMember("target").invoke(getterMethod.getName()).returnValue())
              .finish();
        }
      }
    }
    setMethod.finish();
  }
}