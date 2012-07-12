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
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.util.Bool;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.databinding.client.BindableProxy;
import org.jboss.errai.databinding.client.Convert;
import org.jboss.errai.databinding.client.NonExistingPropertyException;
import org.jboss.errai.databinding.client.api.Bindable;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.InitialState;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

/**
 * Generates a proxy for a {@link Bindable} type.
 * 
 * <p>
 * The proxy will:
 * <ul>
 * <li>Carry out an initial state sync between the bound widgets and the target model, if specified (see
 * {@link DataBinder#DataBinder(Object, InitialState)})</li>
 * <li>Update the bound widget when a setter method is invoked (works for widgets that either implement {@link HasValue}
 * or {@link HasText})</li>
 * <li>Update the target model's state in response to value change events (only works for widgets that implement
 * {@link HasValue})</li>
 * <ul>
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class BindableProxyGenerator {
  private final Class<?> bindable;

  public BindableProxyGenerator(Class<?> bindable) {
    this.bindable = bindable;
  }

  public ClassStructureBuilder<?> generate() {
    @SuppressWarnings("serial")
    ClassStructureBuilder<?> classBuilder =
        ClassBuilder.define(bindable.getSimpleName() + "Proxy", bindable)
            .packageScope()
            .implementsInterface(BindableProxy.class)
            .body()
            .privateField("bindings", MetaClassFactory.get(new TypeLiteral<Map<String, Widget>>() {}))
            .initializesWith(Stmt.newObject(new TypeLiteral<HashMap<String, Widget>>() {}))
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
            .publicMethod(void.class, "setModel", Parameter.of(Object.class, "target"),
                Parameter.of(InitialState.class, "initialState"))
            .append(Stmt.loadClassMember("target").assignValue(Stmt.castTo(bindable, Stmt.loadVariable("target"))))
            .append(Stmt.loadClassMember("initialState").assignValue(Variable.get("initialState")))
            .append(Stmt.loadVariable("this").invoke("syncState", Variable.get("initialState")))
            .finish()
            .publicMethod(void.class, "updateWidgets")
            .append(Stmt.loadVariable("this").invoke("syncState", Stmt.loadStatic(InitialState.class, "FROM_MODEL")))
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
      generateInitialStateSyncMethods(classBuilder);
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
    BlockBuilder<?> bindMethodBuilder =
        classBuilder.publicMethod(void.class, "bind", Parameter.of(Widget.class, "widget", true),
            Parameter.of(String.class, "property", true))
            .append(Stmt.loadVariable("this").invoke("unbind", Variable.get("property")))
            .append(
                Stmt.if_(Bool.expr(Stmt.loadVariable("bindings").invoke("containsValue", Variable.get("widget"))))
                    .append(Stmt.throw_(RuntimeException.class, "Widget already bound to a different property!"))
                    .finish()
            )
            .append(Stmt.loadClassMember("bindings").invoke("put", Variable.get("property"), Variable.get("widget")))
            .append(
                Stmt.if_(Bool.instanceOf(Variable.get("widget"), MetaClassFactory.getAsStatement(HasValue.class)))
                    .append(
                        Stmt.loadClassMember("handlerRegistrations").invoke(
                            "put",
                            Variable.get("property"),
                            Stmt.castTo(HasValue.class, Stmt.loadVariable("widget")).invoke(
                                "addValueChangeHandler",
                                Stmt.newObject(ValueChangeHandler.class).extend()
                                    .publicOverridesMethod("onValueChange",
                                        Parameter.of(ValueChangeEvent.class, "event"))
                                    .append(
                                        Stmt.loadStatic(classBuilder.getClassDefinition(), "this").invoke("set",
                                            Variable.get("property"),
                                            Stmt.nestedCall(Stmt.loadVariable("event").invoke("getValue"))))
                                    .finish()
                                    .finish()
                                )
                            )
                    )
                    .finish()
            )
            .append(
                Stmt.loadVariable("this").invoke("syncState", Variable.get("widget"), Variable.get("property"),
                    Variable.get("initialState")));

    bindMethodBuilder.finish();
  }

  private void generateInitialStateSyncMethods(ClassStructureBuilder<?> classBuilder) {
    classBuilder.privateMethod(void.class, "syncState", Parameter.of(InitialState.class, "initialState", true))
        .append(
            Stmt.loadVariable("bindings").invoke("keySet").foreach("property")
                .append(
                    Stmt.try_()
                        .append(
                            Stmt.loadVariable("this")
                                .invoke("syncState",
                                    Stmt.loadVariable("bindings").invoke("get", Variable.get("property")),
                                    Stmt.castTo(String.class, Stmt.loadVariable("property")),
                                    Stmt.loadVariable("initialState")))
                        .finish()
                        .catch_(NonExistingPropertyException.class, "e")
                        .append(Stmt.invokeStatic(GWT.class, "log", Stmt.loadVariable("e")
                            .invoke("createErrorMessage", "Skipping state synchronization for unknown property:")))
                        .finish())
                .finish())
         .finish();

    classBuilder.privateMethod(void.class, "syncState", Parameter.of(Widget.class, "widget", true),
        Parameter.of(String.class, "property", true), Parameter.of(InitialState.class, "initialState", true))
        .append(
            Stmt.if_(Bool.isNotNull(Variable.get("initialState")))
                .append(Stmt.declareVariable("value", Object.class, null))
                .append(
                    Stmt.if_(Bool.instanceOf(Variable.get("widget"), MetaClassFactory.getAsStatement(HasValue.class)))
                        .append(Stmt.declareVariable("hasValue", HasValue.class,
                            Stmt.castTo(HasValue.class, Stmt.loadVariable("widget"))))
                        .append(Stmt.loadVariable("value").assignValue(
                            Stmt.loadVariable("initialState").invoke("getInitialValue",
                                Stmt.loadVariable("this").invoke("get", Variable.get("property")),
                                Stmt.loadVariable("hasValue").invoke("getValue"))))
                        .append(
                            Stmt.loadVariable("hasValue").invoke(
                                "setValue",
                                Stmt.invokeStatic(Convert.class, "to",
                                    Stmt.castTo(HasValue.class,
                                        Stmt.loadVariable("widget")).invoke("getValue").invoke("getClass"),
                                        Stmt.loadVariable("value"))))
                        .finish()
                        .elseif_(
                            Bool.instanceOf(Variable.get("widget"), MetaClassFactory.getAsStatement(HasText.class)))
                        .append(
                            Stmt.declareVariable("hasText", HasText.class,
                                Stmt.castTo(HasText.class, Stmt.loadVariable("widget"))))
                        .append(Stmt.loadVariable("value").assignValue(
                            Stmt.loadVariable("initialState").invoke("getInitialValue",
                                Stmt.loadVariable("this").invoke("get", Variable.get("property")),
                                Stmt.loadVariable("hasText").invoke("getText"))))
                        .append(
                            Stmt.loadVariable("hasText").invoke(
                                "setText",
                                Stmt.castTo(String.class, Stmt.invokeStatic(Convert.class, "to", String.class, Stmt
                                    .loadVariable("value")))))
                        .finish()
                )
                .append(Stmt.loadVariable("this").invoke("set", Variable.get("property"), Variable.get("value")))
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

    BlockBuilder<?> getMethod = classBuilder.publicMethod(Object.class, "get",
        Parameter.of(String.class, "property"));

    BlockBuilder<?> setMethod = classBuilder.publicMethod(void.class, "set",
        Parameter.of(String.class, "property"),
        Parameter.of(Object.class, "value"));

    if (propertyDescriptors != null) {
      for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
        generateGetter(classBuilder, propertyDescriptor, getMethod);
        generateSetter(classBuilder, propertyDescriptor, setMethod);
      }
    }
    getMethod.append(Stmt.throw_(NonExistingPropertyException.class, Variable.get("property"))).finish();
    setMethod.finish();
  }

  private void generateGetter(ClassStructureBuilder<?> classBuilder, PropertyDescriptor propertyDescriptor,
      BlockBuilder<?> getMethod) {

    Method getterMethod = propertyDescriptor.getReadMethod();
    if (getterMethod != null && !Modifier.isFinal(getterMethod.getModifiers())) {
      getMethod
          .append(Stmt
              .if_(Bool.expr(Stmt.loadVariable("property").invoke("equals", propertyDescriptor.getName())))
              .append(
                  Stmt.loadVariable("this").invoke(getterMethod.getName()).returnValue())
              .finish()
          );

      classBuilder.publicMethod(getterMethod.getReturnType(), getterMethod.getName())
          .append(Stmt.loadClassMember("target").invoke(getterMethod.getName()).returnValue())
          .finish();
    }
  }

  private void generateSetter(ClassStructureBuilder<?> classBuilder, PropertyDescriptor propertyDescriptor,
      BlockBuilder<?> setMethod) {

    Method setterMethod = propertyDescriptor.getWriteMethod();
    if (setterMethod != null && !Modifier.isFinal(setterMethod.getModifiers())) {
      setMethod
          .append(Stmt
              .if_(Bool.expr(Stmt.loadVariable("property").invoke("equals", propertyDescriptor.getName())))
              .append(
                  Stmt.loadVariable("target").invoke(
                      setterMethod.getName(),
                      Cast.to(MetaClassFactory.get(setterMethod.getParameterTypes()[0]).asBoxed(),
                          Stmt.invokeStatic(Convert.class, "to",
                              MetaClassFactory.get(setterMethod.getParameterTypes()[0]).asBoxed().asClass(),
                              Stmt.loadVariable("value")))))
              .finish()
          );

      MetaClass boxedParmType = MetaClassFactory.get(setterMethod.getParameterTypes()[0]).asBoxed();
      classBuilder.publicMethod(setterMethod.getReturnType(), setterMethod.getName(),
          Parameter.of(setterMethod.getParameterTypes()[0], propertyDescriptor.getName()))
          .append(Stmt.loadClassMember("target").invoke(setterMethod.getName(),
                    Cast.to(setterMethod.getParameterTypes()[0], Stmt.loadVariable(propertyDescriptor.getName()))))
          .append(
              Stmt.if_(
                  Bool.expr(Stmt.loadClassMember("bindings").invoke("containsKey", propertyDescriptor.getName())))
                  .append(Stmt.declareVariable("widget", Widget.class,
                      Stmt.loadClassMember("bindings").invoke("get", propertyDescriptor.getName())))
                  .append(
                      Stmt.if_(
                          Bool.instanceOf(Variable.get("widget"), MetaClassFactory.getAsStatement(HasValue.class)))
                          .append(
                              Stmt.castTo(HasValue.class, Stmt.loadVariable("widget")).invoke(
                                  "setValue",
                                  Stmt.invokeStatic(Convert.class, "to",
                                      Stmt.castTo(HasValue.class, Stmt.loadVariable("widget")).invoke("getValue")
                                          .invoke("getClass"),
                                      Stmt.loadVariable(propertyDescriptor.getName())), true))
                          .finish()
                          .elseif_(
                              Bool.instanceOf(Variable.get("widget"), MetaClassFactory.getAsStatement(HasText.class)))
                          .append(
                              Stmt.castTo(HasText.class, Stmt.loadVariable("widget"))
                                  .invoke(
                                      "setText",
                                      Stmt.castTo(String.class,
                                          Stmt.invokeStatic(Convert.class, "to", String.class,
                                              Stmt.castTo(boxedParmType, Stmt
                                                  .loadVariable(propertyDescriptor.getName()))
                                              )
                                          )
                                  )
                          )
                          .finish()
                  ).finish())
          .finish();
    }
  }
}