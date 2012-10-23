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

import static org.jboss.errai.codegen.meta.MetaClassFactory.parameterizedAs;
import static org.jboss.errai.codegen.meta.MetaClassFactory.typeParametersOf;

import org.jboss.errai.codegen.BlockStatement;
import org.jboss.errai.codegen.Cast;
import org.jboss.errai.codegen.Context;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.Variable;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.ContextualStatementBuilder;
import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.util.Bool;
import org.jboss.errai.codegen.util.EmptyStatement;
import org.jboss.errai.codegen.util.If;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.databinding.client.BindableProxy;
import org.jboss.errai.databinding.client.BindableProxyAgent;
import org.jboss.errai.databinding.client.NonExistingPropertyException;
import org.jboss.errai.databinding.client.PropertyType;
import org.jboss.errai.databinding.client.api.Bindable;
import org.jboss.errai.databinding.client.api.InitialState;

/**
 * Generates a proxy for a {@link Bindable} type.
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
        .body();

    classBuilder
        .privateField("agent", parameterizedAs(BindableProxyAgent.class, typeParametersOf(bindable)))
        .finish()
        .publicConstructor(Parameter.of(InitialState.class, "initialState"))
        .callThis(Stmt.newObject(bindable), Variable.get("initialState"))
        .finish()
        .publicConstructor(Parameter.of(bindable, "target"), Parameter.of(InitialState.class, "initialState"))
        .append(Stmt.loadVariable("agent").assignValue(
            Stmt.newObject(parameterizedAs(BindableProxyAgent.class, typeParametersOf(bindable)),
                Variable.get("this"), Variable.get("target"), Variable.get("initialState"))))
        .append(generatePropertiesMap())
        .finish()
        .publicMethod(BindableProxyAgent.class, "getAgent")
        .append(agent().returnValue())
        .finish()
        .publicMethod(void.class, "updateWidgets")
        .append(agent().invoke("syncState", Stmt.loadStatic(InitialState.class, "FROM_MODEL")))
        .finish()
        .publicMethod(bindable, "unwrap")
        .append(target().returnValue())
        .finish()
        .publicMethod(boolean.class, "equals", Parameter.of(Object.class, "obj"))
        .append(
            If.instanceOf(Variable.get("obj"), classBuilder.getClassDefinition())
                .append(Stmt.loadVariable("obj").assignValue(
                    Stmt.castTo(classBuilder.getClassDefinition(), Variable.get("obj")).invoke("unwrap")))
                .finish())
        .append(target().invoke("equals", Variable.get("obj")).returnValue())
        .finish()
        .publicMethod(int.class, "hashCode")
        .append(target().invoke("hashCode").returnValue())
        .finish()
        .publicMethod(String.class, "toString")
        .append(target().invoke("toString").returnValue())
        .finish();

    generateProxyAccessorMethods(classBuilder);

    return classBuilder;
  }

  private void generateProxyAccessorMethods(ClassStructureBuilder<?> classBuilder) {
    BlockBuilder<?> getMethod = classBuilder.publicMethod(Object.class, "get",
        Parameter.of(String.class, "property"));

    BlockBuilder<?> setMethod = classBuilder.publicMethod(void.class, "set",
            Parameter.of(String.class, "property"),
            Parameter.of(Object.class, "value"));

    for (String property : bindable.getBeanDescriptor().getProperties()) {
      generateGetter(classBuilder, property, getMethod);
      generateSetter(classBuilder, property, setMethod);
    }

    Statement nonExistingPropertyException = Stmt.throw_(NonExistingPropertyException.class, Variable.get("property"));
    getMethod.append(nonExistingPropertyException).finish();
    setMethod.append(nonExistingPropertyException).finish();
  }

  private void generateGetter(ClassStructureBuilder<?> classBuilder, String property,
      BlockBuilder<?> getMethod) {

    MetaMethod getterMethod = bindable.getBeanDescriptor().getReadMethodForProperty(property);
    if (getterMethod != null && !getterMethod.isFinal()) {
      getMethod.append(
          If.objEquals(Stmt.loadVariable("property"), property)
              .append(Stmt.loadVariable("this").invoke(getterMethod.getName()).returnValue())
              .finish()
          );

      classBuilder.publicMethod(getterMethod.getReturnType(), getterMethod.getName())
          .append(target().invoke(getterMethod.getName()).returnValue())
          .finish();
    }
  }

  private void generateSetter(ClassStructureBuilder<?> classBuilder, String property, BlockBuilder<?> setMethod) {
    MetaMethod getterMethod = bindable.getBeanDescriptor().getReadMethodForProperty(property);
    MetaMethod setterMethod = bindable.getBeanDescriptor().getWriteMethodForProperty(property);
    if (setterMethod != null && !setterMethod.isFinal()) {
      setMethod.append(
          If.cond(Stmt.loadVariable("property").invoke("equals", property))
              .append(
                  target().invoke(setterMethod.getName(),
                      Cast.to(setterMethod.getParameters()[0].getType().asBoxed(), Variable.get("value"))))
              .append(
                  Stmt.returnVoid())
              .finish()
          );

      MetaClass paramType = setterMethod.getParameters()[0].getType();

      Statement callSetterOnTarget =
          target().invoke(setterMethod.getName(), Cast.to(paramType, Stmt.loadVariable(property)));
      // If the set method we are proxying returns a value, capture that value into a local variable
      Statement returnValueOfSetter = EmptyStatement.INSTANCE;
      if (!setterMethod.getReturnType().equals(MetaClassFactory.get(void.class))) {
        callSetterOnTarget =
            Stmt.declareFinalVariable("returnValueOfSetter", setterMethod.getReturnType(), callSetterOnTarget);
        returnValueOfSetter = Stmt.nestedCall(Refs.get("returnValueOfSetter")).returnValue();
      }

      Statement updateNestedProxy = EmptyStatement.INSTANCE;
      if (paramType.isAnnotationPresent(Bindable.class)) {
        updateNestedProxy =
            Stmt.if_(Bool.expr(field("binders").invoke("containsKey", property)))
                .append(Stmt.loadVariable(property).assignValue(Cast.to(paramType,
                    field("binders").invoke("get", property).invoke("setModel", Variable.get(property)))))
                .append(Stmt.loadVariable("this").invoke("set", property, Variable.get(property)))
                .finish();
      }

      classBuilder.publicMethod(setterMethod.getReturnType(), setterMethod.getName(),
          Parameter.of(paramType, property))
          .append(updateNestedProxy)
          .append(
              Stmt.declareVariable("oldValue", paramType, target().invoke(getterMethod.getName())))
          .append(callSetterOnTarget)
          .append(
              agent().invoke("updateWidgetAndFireEvents", property, Variable.get("oldValue"), Variable.get(property)))
          .append(returnValueOfSetter)
          .finish();
    }
  }

  private Statement generatePropertiesMap() {
    BlockStatement block = new BlockStatement();
    for (String property : bindable.getBeanDescriptor().getProperties()) {
      MetaMethod readMethod = bindable.getBeanDescriptor().getReadMethodForProperty(property);
      if (!readMethod.isFinal()) {
        block.addStatement(field("propertyTypes").invoke(
            "put",
            property,
            Stmt.newObject(PropertyType.class, readMethod.getReturnType().asBoxed().asClass(),
                readMethod.getReturnType().isAnnotationPresent(Bindable.class))
            )
            );
      }
    }
    return block;
  }

  private ContextualStatementBuilder field(String field) {
    return agent().loadField(field);
  }

  private ContextualStatementBuilder agent() {
    return Stmt.loadClassMember("agent");
  }

  private ContextualStatementBuilder target() {
    return Stmt.nestedCall(new Statement() {
      @Override
      public String generate(Context context) {
        return agent().loadField("target").generate(context);
      }

      @Override
      public MetaClass getType() {
        return bindable;
      }
    });
  }
}