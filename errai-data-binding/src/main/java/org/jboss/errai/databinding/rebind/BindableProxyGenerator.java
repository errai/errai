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
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.util.TypeLiteral;

import org.jboss.errai.codegen.Cast;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Variable;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.util.Bool;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.common.client.api.WrappedPortable;
import org.jboss.errai.databinding.client.BindableProxy;
import org.jboss.errai.databinding.client.api.Bindable;
import org.jboss.errai.databinding.client.api.InitialState;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
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
    @SuppressWarnings("serial")
    ClassStructureBuilder<?> classBuilder =
        ClassBuilder.define(bindable.getSimpleName() + "Proxy", bindable)
            .packageScope()
            .implementsInterface(BindableProxy.class)
            .implementsInterface(WrappedPortable.class)
            .body()
            .privateField("bindings", MetaClassFactory.get(new TypeLiteral<Map<String, HasValue>>() {}))
            .initializesWith(Stmt.newObject(new TypeLiteral<HashMap<String, HasValue>>() {}))
            .finish()
            .privateField("handlerRegistrations",
                MetaClassFactory.get(new TypeLiteral<Map<String, HandlerRegistration>>() {}))
            .initializesWith(Stmt.newObject(new TypeLiteral<HashMap<String, HandlerRegistration>>() {}))
            .finish()
            .privateField("target", bindable)
            .finish()
            .privateField("initialState", InitialState.class)
            .finish()
            .publicConstructor(Parameter.of(InitialState.class, "initialState"))
            .append(Stmt.loadClassMember("target").assignValue(Stmt.newObject(bindable)))
            .append(Stmt.loadClassMember("initialState").assignValue(Variable.get("initialState")))
            .finish()
            .publicConstructor(Parameter.of(bindable, "target"), Parameter.of(InitialState.class, "initialState"))
            .append(Stmt.loadClassMember("target").assignValue(Variable.get("target")))
            .append(Stmt.loadClassMember("initialState").assignValue(Variable.get("initialState")))
            .finish()
            .publicMethod(bindable, "unwrap")
            .append(Stmt.loadClassMember("target").returnValue())
            .finish()
            .publicMethod(boolean.class, "equals", Parameter.of(Object.class, "obj"))
            .append(Stmt.loadClassMember("target").invoke("equals", Variable.get("obj")).returnValue())
            .finish()
            .publicMethod(int.class, "hashCode")
            .append(Stmt.loadClassMember("target").invoke("hashCode").returnValue())
            .finish();

    BeanInfo beanInfo;
    try {
      beanInfo = Introspector.getBeanInfo(bindable);
      generateProxyBindMethod(classBuilder);
      generateProxyUnbindMethods(classBuilder);
      generateProxyAccessorMethods(beanInfo.getPropertyDescriptors(), classBuilder);
    }
    catch (IntrospectionException e) {
      throw new RuntimeException("Failed to introspect bean:" + bindable.getName(), e);
    }

    return classBuilder;
  }

  private void generateProxyBindMethod(ClassStructureBuilder<?> classBuilder) {
    classBuilder.publicMethod(void.class, "bind", Parameter.of(HasValue.class, "widget", true),
        Parameter.of(String.class, "property", true))
        .append(Stmt.loadVariable("this").invoke("unbind", Variable.get("property")))
        .append(Stmt.loadClassMember("bindings").invoke("put", Variable.get("property"), Variable.get("widget")))
        .append(
            Stmt.loadClassMember("handlerRegistrations").invoke(
                "put",
                Variable.get("property"),
                Stmt.loadVariable("widget").invoke(
                    "addValueChangeHandler",
                    Stmt.newObject(ValueChangeHandler.class).extend()
                        .publicOverridesMethod("onValueChange", Parameter.of(ValueChangeEvent.class, "event"))
                        .append(
                            Stmt.loadStatic(classBuilder.getClassDefinition(), "this").invoke("set",
                                Variable.get("property"),
                                Stmt.nestedCall(Stmt.loadVariable("event").invoke("getValue"))))
                        .finish()
                        .finish()
                    )
                )
            )
        .append(
            Stmt.if_(Bool.isNotNull(Variable.get("initialState")))
                .append(
                    Stmt.loadVariable("widget").invoke(
                        "setValue",
                        Stmt.loadVariable("initialState").invoke("getInitialValue", Variable.get("target"),
                            Variable.get("widget"))))
                .append(
                    Stmt.loadVariable("this").invoke(
                        "set",
                        Variable.get("property"),
                        Stmt.loadVariable("initialState").invoke("getInitialValue", Variable.get("target"),
                            Variable.get("widget"))))
                .finish()
            )
        .finish();
  }

  private void generateProxyUnbindMethods(ClassStructureBuilder<?> classBuilder) {
    classBuilder.publicMethod(void.class, "unbind", Parameter.of(String.class, "property"))
        .append(Stmt.loadClassMember("bindings").invoke("remove", Variable.get("property")))
        .append(Stmt.declareVariable("reg", HandlerRegistration.class,
            Stmt.loadClassMember("handlerRegistrations").invoke("remove", Variable.get("property"))))
        .append(Stmt.if_(Bool.isNotNull(Variable.get("reg")))
            .append(Stmt.loadVariable("reg").invoke("removeHandler"))
            .finish())
        .finish();

    classBuilder.publicMethod(void.class, "unbind")
        .append(
            Stmt.loadVariable("handlerRegistrations").invoke("keySet").foreach("reg")
                .append(
                    Stmt.castTo(HandlerRegistration.class,
                        Stmt.loadVariable("handlerRegistrations").invoke("get", Variable.get("reg")))
                          .invoke("removeHandler"))
                .finish())
        .append(Stmt.loadClassMember("bindings").invoke("clear"))
        .append(Stmt.loadClassMember("handlerRegistrations").invoke("clear"))
        .finish();
  }

  private void generateProxyAccessorMethods(PropertyDescriptor[] propertyDescriptors,
      ClassStructureBuilder<?> classBuilder) {

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
                  .append(
                      Stmt.loadVariable("target").invoke(
                          setterMethod.getName(),
                          Cast.to(MetaClassFactory.get(setterMethod.getParameterTypes()[0]).asBoxed(),
                              Stmt.loadVariable("value"))))
                  .finish()
              );

          classBuilder.publicMethod(setterMethod.getReturnType(), setterMethod.getName(),
              Parameter.of(setterMethod.getParameterTypes()[0], propertyDescriptor.getName()))
              .append(Stmt.loadClassMember("target").invoke(setterMethod.getName(),
                        Cast.to(setterMethod.getParameterTypes()[0], Stmt.loadVariable(propertyDescriptor.getName()))))
              .append(
                  Stmt.if_(
                      Bool.expr(Stmt.loadClassMember("bindings").invoke("containsKey", propertyDescriptor.getName())))
                      .append(
                          Stmt.nestedCall(
                              Cast.to(HasValue.class, Stmt.loadClassMember("bindings").invoke("get",
                                  propertyDescriptor.getName())))
                              .invoke("setValue", Stmt.loadVariable(propertyDescriptor.getName()), true))
                      .finish())
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