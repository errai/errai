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
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.Variable;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.util.Bool;
import org.jboss.errai.codegen.util.If;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.databinding.client.BindableProxy;
import org.jboss.errai.databinding.client.HasProperties;
import org.jboss.errai.databinding.client.NonExistingPropertyException;
import org.jboss.errai.databinding.client.api.Bindable;
import org.jboss.errai.databinding.client.api.Convert;
import org.jboss.errai.databinding.client.api.Converter;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.InitialState;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.databinding.client.*;

/**
 * Generates a proxy for a {@link Bindable} type.
 * <p>
 * The proxy will:
 * <ul>
 * <li>Carry out an initial state sync between the bound widgets and the target model, if specified (see
 * {@link DataBinder#DataBinder(Object, InitialState)})</li>
 * <li>Update the bound widget when a setter method is invoked on the model (works for widgets that either implement
 * {@link HasValue} or {@link HasText})</li>
 * <li>Update the target model's state in response to value change events (only works for widgets that implement
 * {@link HasValue})</li>
 * <ul>
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class BindableProxyGenerator {
  private final MetaClass bindable;

  public BindableProxyGenerator(MetaClass bindable) {
    this.bindable = bindable;
  }

  public ClassStructureBuilder<?> generate() {
    ClassStructureBuilder<?> classBuilder = ClassBuilder.define(bindable.getName() + "Proxy", bindable)
        .packageScope()
        .implementsInterface(BindableProxy.class)
        .implementsInterface(HasProperties.class)
        .implementsInterface(HasPropertyChangeHandlers.class)
        .body();
    addPrivateFields(classBuilder);

    classBuilder
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
            .publicMethod(Widget.class, "getWidget", Parameter.of(String.class, "property"))
            .append(Stmt.loadVariable("bindings").invoke("get", Variable.get("property")).returnValue())
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
            .finish()
            .publicMethod(String.class, "toString")
            .append(Stmt.loadClassMember("target").invoke("toString").returnValue())
            .finish();

    BeanInfo beanInfo;
    try {
      beanInfo = Introspector.getBeanInfo(bindable.asClass());
      generateStateSyncMethods(classBuilder);
      generateProxyBindMethod(classBuilder);
      generateProxyUnbindMethods(classBuilder);
      generateProxyAccessorMethods(beanInfo.getPropertyDescriptors(), classBuilder);
      generateHasPropertyChangeHandlersMethods(classBuilder);
    }
    catch (IntrospectionException e) {
      throw new RuntimeException("Failed to introspect bean:" + bindable.getName(), e);
    }

    return classBuilder;
  }

  @SuppressWarnings({ "serial", "rawtypes" })
  private void addPrivateFields(ClassStructureBuilder<?> classBuilder) {
    classBuilder
        .privateField("bindings", MetaClassFactory.get(new TypeLiteral<Map<String, Widget>>() {}))
        .initializesWith(Stmt.newObject(new TypeLiteral<HashMap<String, Widget>>() {}))
        .finish()
        .privateField("converters", MetaClassFactory.get(new TypeLiteral<Map<String, Converter>>() {}))
        .initializesWith(Stmt.newObject(new TypeLiteral<HashMap<String, Converter>>() {}))
        .finish()
        .privateField("handlerRegistrations",
            MetaClassFactory.get(new TypeLiteral<Map<String, HandlerRegistration>>() {}))
        .initializesWith(Stmt.newObject(new TypeLiteral<HashMap<String, HandlerRegistration>>() {}))
        .finish()
        .privateField("target", bindable)
        .finish()
        .privateField("initialState", InitialState.class)
        .finish()
        .privateField("propertyChangeHandlerSupport", PropertyChangeHandlerSupport.class)
        .initializesWith(Stmt.newObject(new TypeLiteral<PropertyChangeHandlerSupport>(){}))
        .finish();
         
    
  }

  private void generateProxyBindMethod(ClassStructureBuilder<?> classBuilder) {
    BlockBuilder<?> bindMethodBuilder =
        classBuilder.publicMethod(void.class, "bind", Parameter.of(Widget.class, "widget", true),
            Parameter.of(String.class, "property", true), Parameter.of(Converter.class, "converter", true))
            .append(
                Stmt.loadVariable("this").invoke("unbind", Variable.get("property")))
            .append(
                If.cond(Stmt.loadVariable("bindings").invoke("containsValue", Variable.get("widget")))
                    .append(Stmt.throw_(RuntimeException.class, "Widget already bound to a different property!"))
                    .finish()
            )
            .append(
                Stmt.loadClassMember("bindings").invoke("put", Variable.get("property"), Variable.get("widget")))
            .append(
                Stmt.loadClassMember("converters").invoke("put", Variable.get("property"), Variable.get("converter")))
            .append(
                If.instanceOf(Variable.get("widget"), HasValue.class)
                    .append(
                        Stmt.loadClassMember("handlerRegistrations").invoke(
                            "put",
                            Variable.get("property"),
                            Stmt.castTo(HasValue.class, Stmt.loadVariable("widget")).invoke("addValueChangeHandler",
                                Stmt.newObject(ValueChangeHandler.class).extend()
                                    .publicOverridesMethod("onValueChange",
                                        Parameter.of(ValueChangeEvent.class, "event"))
                                    .append(Stmt.try_()
                                        .append(
                                            Stmt.loadStatic(classBuilder.getClassDefinition(), "this").invoke("set",
                                                Variable.get("property"),
                                                Stmt.nestedCall(Stmt.loadVariable("event").invoke("getValue"))))
                                         .finish()
                                         .catch_(NonExistingPropertyException.class, "e")
                                         .append(Stmt.invokeStatic(GWT.class, "log", Stmt.loadVariable("e")
                                             .invoke("createErrorMessage", "Cannot update unknown property:")))
                                         .finish()
                                    ).finish()
                                    .finish()
                                )
                            )
                    ).finish()
            )
            .append(
                Stmt.loadVariable("this").invoke("syncState", Variable.get("widget"), Variable.get("property"),
                    Variable.get("initialState")));

    bindMethodBuilder.finish();
  }

  private void generateStateSyncMethods(ClassStructureBuilder<?> classBuilder) {
    generateStateSyncForBindings(classBuilder);

    classBuilder.privateMethod(void.class, "syncState", Parameter.of(Widget.class, "widget", true),
        Parameter.of(String.class, "property", true), Parameter.of(InitialState.class, "initialState", true))
        .append(
            If.isNotNull(Variable.get("initialState"))
                .append(
                    Stmt.try_()
                        .append(Stmt.declareVariable("value", Object.class, null))
                        .append(
                            If.instanceOf(Variable.get("widget"), HasValue.class)
                                .append(
                                    Stmt.declareVariable("hasValue", HasValue.class,
                                        Stmt.castTo(HasValue.class, Stmt.loadVariable("widget"))))
                                .append(
                                    Stmt.loadVariable("value").assignValue(
                                        Stmt.loadVariable("initialState").invoke("getInitialValue",
                                            Stmt.loadVariable("this").invoke("get", Variable.get("property")),
                                            Stmt.loadVariable("hasValue").invoke("getValue"))))
                                .append(
                                    Stmt.loadVariable("hasValue").invoke(
                                        "setValue",
                                        Stmt.invokeStatic(Convert.class, "toWidgetValue",
                                            Stmt.castTo(HasValue.class,
                                                Stmt.loadVariable("widget")).invoke("getValue").invoke("getClass"),
                                            Stmt.loadVariable("value"),
                                            Stmt.loadVariable("converters").invoke("get", Variable.get("property")))))
                                .finish()
                                .elseif_(
                                    Bool.instanceOf(Variable.get("widget"), HasText.class))
                                .append(
                                    Stmt.declareVariable("hasText", HasText.class,
                                        Stmt.castTo(HasText.class, Stmt.loadVariable("widget"))))
                                .append(
                                    Stmt.loadVariable("value").assignValue(
                                        Stmt.loadVariable("initialState").invoke("getInitialValue",
                                            Stmt.loadVariable("this").invoke("get", Variable.get("property")),
                                            Stmt.loadVariable("hasText").invoke("getText"))))
                                .append(
                                    Stmt.loadVariable("hasText").invoke(
                                        "setText",
                                        Stmt.castTo(String.class, Stmt.invokeStatic(Convert.class, "toWidgetValue",
                                            String.class,
                                            Stmt.loadVariable("value"),
                                            Stmt.loadVariable("converters").invoke("get", Variable.get("property"))))))
                                .finish()
                        )
                        .append(
                            Stmt.loadVariable("this").invoke("set", Variable.get("property"), Variable.get("value")))
                        .finish()
                        .catch_(NonExistingPropertyException.class, "e")
                        .append(Stmt.invokeStatic(GWT.class, "log", Stmt.loadVariable("e")
                            .invoke("createErrorMessage", "Skipping state synchronization for unknown property:")))
                        .finish())
                .finish()
        )
        .finish();
  }

  private void generateStateSyncForBindings(ClassStructureBuilder<?> classBuilder) {
    classBuilder.privateMethod(void.class, "syncState", Parameter.of(InitialState.class, "initialState", true))
        .append(
            Stmt.loadVariable("bindings").invoke("keySet").foreach("property")
                .append(
                    Stmt.loadVariable("this")
                        .invoke("syncState",
                            Stmt.loadVariable("bindings").invoke("get", Variable.get("property")),
                            Stmt.castTo(String.class, Stmt.loadVariable("property")),
                            Stmt.loadVariable("initialState")))
                .finish())
        .finish();
  }

  private void generateProxyUnbindMethods(ClassStructureBuilder<?> classBuilder) {
    classBuilder.publicMethod(void.class, "unbind", Parameter.of(String.class, "property"))
        .append(Stmt.loadClassMember("bindings").invoke("remove", Variable.get("property")))
        .append(Stmt.loadClassMember("converters").invoke("remove", Variable.get("property")))
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
        .append(Stmt.loadClassMember("converters").invoke("clear"))
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

    Statement nonExistingPropertyException = Stmt.throw_(NonExistingPropertyException.class, Variable.get("property"));
    getMethod.append(nonExistingPropertyException).finish();
    setMethod.append(nonExistingPropertyException).finish();
  }

  private void generateGetter(ClassStructureBuilder<?> classBuilder, PropertyDescriptor propertyDescriptor,
      BlockBuilder<?> getMethod) {

    Method getterMethod = propertyDescriptor.getReadMethod();
    if (getterMethod != null && !Modifier.isFinal(getterMethod.getModifiers())) {
      getMethod
          .append(
              If.objEquals(Stmt.loadVariable("property"), propertyDescriptor.getName())
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
          .append(
             If.cond(Stmt.loadVariable("property").invoke("equals", propertyDescriptor.getName()))
                 .append(
                        Stmt.declareVariable("oldValue", MetaClassFactory.get(setterMethod.getParameterTypes()[0]).asBoxed(),
                        Stmt.loadVariable("target").invoke(propertyDescriptor.getReadMethod().getName())))
                 .append(
                     Stmt.loadVariable("target").invoke(
                         setterMethod.getName(),
                         Cast.to(MetaClassFactory.get(setterMethod.getParameterTypes()[0]).asBoxed(),
                             Stmt.invokeStatic(Convert.class, "toModelValue",
                                 MetaClassFactory.get(setterMethod.getParameterTypes()[0]).asBoxed().asClass(),
                                 Stmt.loadVariable("value"),
                                 Stmt.loadVariable("converters").invoke("get", Variable.get("property"))))))
                 .append(Stmt.loadVariable("propertyChangeHandlerSupport").invoke("notifyHandlers",
                         Stmt.newObject(PropertyChangeEvent.class, Stmt.loadVariable("property"), Stmt.loadVariable("oldValue"), Stmt.loadVariable("value"))))
                 .append(Stmt.returnVoid())
                 .finish()
          );

      MetaClass boxedParmType = MetaClassFactory.get(setterMethod.getParameterTypes()[0]).asBoxed();
      classBuilder.publicMethod(setterMethod.getReturnType(), setterMethod.getName(),
          Parameter.of(setterMethod.getParameterTypes()[0], propertyDescriptor.getName()))
          .append(Stmt.declareVariable("oldValue", Object.class, Stmt.loadClassMember("target").invoke(propertyDescriptor.getReadMethod().getName()) ))
          .append(Stmt.loadClassMember("target").invoke(setterMethod.getName(),
                    Cast.to(setterMethod.getParameterTypes()[0], Stmt.loadVariable(propertyDescriptor.getName()))))
          .append(
              If.cond(Stmt.loadClassMember("bindings").invoke("containsKey", propertyDescriptor.getName()))
                  .append(Stmt.declareVariable("widget", Widget.class,
                      Stmt.loadClassMember("bindings").invoke("get", propertyDescriptor.getName())))
                  .append(
                      If.instanceOf(Variable.get("widget"), HasValue.class)
                          .append(
                              Stmt.castTo(HasValue.class, Stmt.loadVariable("widget")).invoke(
                                  "setValue",
                                  Stmt.invokeStatic(Convert.class, "toWidgetValue",
                                      Stmt.castTo(HasValue.class, Stmt.loadVariable("widget")).invoke("getValue")
                                          .invoke("getClass"),
                                      Stmt.loadVariable(propertyDescriptor.getName()),
                                      Stmt.loadVariable("converters").invoke("get", propertyDescriptor.getName())),
                                  true))
                          .finish()
                          .elseif_(
                              Bool.instanceOf(Variable.get("widget"), HasText.class))
                          .append(
                              Stmt.castTo(HasText.class, Stmt.loadVariable("widget"))
                                  .invoke(
                                      "setText",
                                      Stmt.castTo(String.class,
                                          Stmt.invokeStatic(Convert.class, "toWidgetValue", String.class,
                                              Stmt.castTo(boxedParmType, Stmt
                                                  .loadVariable(propertyDescriptor.getName())),
                                              Stmt.loadVariable("converters").invoke("get",
                                                  propertyDescriptor.getName())
                                              )
                                          )
                                  )
                          )
                          .finish()
                  ).finish())
            .append(Stmt.declareVariable("event", PropertyChangeEvent.class, 
                 Stmt.newObject(PropertyChangeEvent.class, 
                      propertyDescriptor.getName(),  Stmt.loadVariable("oldValue"), Stmt.loadVariable(propertyDescriptor.getName())
                    )
                 ))
            .append(Stmt.loadVariable("propertyChangeHandlerSupport").invoke("notifyHandlers", Stmt.loadVariable("event") ))
          .finish();
    }
  }
  
  private void generateHasPropertyChangeHandlersMethods(ClassStructureBuilder<?> classBuilder){
       classBuilder.publicMethod(void.class, "addPropertyChangeHandler", Parameter.of(PropertyChangeHandler.class, "handler"))
         .append(Stmt.loadClassMember("propertyChangeHandlerSupport").invoke("addPropertyChangeHandler", Variable.get("handler")))
         .finish();
       
        classBuilder.publicMethod(void.class, "removePropertyChangeHandler", Parameter.of(PropertyChangeHandler.class, "handler"))
          .append(Stmt.loadClassMember("propertyChangeHandlerSupport").invoke("removePropertyChangeHandler", Variable.get("handler")))
          .finish();
  }
}