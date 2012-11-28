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

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.codegen.BlockStatement;
import org.jboss.errai.codegen.Cast;
import org.jboss.errai.codegen.Context;
import org.jboss.errai.codegen.DefParameters;
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
import org.jboss.errai.databinding.client.HasProperties;
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
  private final String agentField;

  public BindableProxyGenerator(MetaClass bindable) {
    this.bindable = bindable;
    this.agentField = inferAgentFieldName();
  }

  public ClassStructureBuilder<?> generate() {
    ClassStructureBuilder<?> classBuilder = ClassBuilder.define(bindable.getName() + "Proxy", bindable)
        .packageScope()
        .implementsInterface(BindableProxy.class)
        .body();

    classBuilder
        .privateField(agentField, parameterizedAs(BindableProxyAgent.class, typeParametersOf(bindable)))
        .finish()
        .publicConstructor(Parameter.of(InitialState.class, "initialState"))
        .callThis(Stmt.newObject(bindable), Variable.get("initialState"))
        .finish()
        .publicConstructor(Parameter.of(bindable, "target"), Parameter.of(InitialState.class, "initialState"))
        .append(Stmt.loadVariable(agentField).assignValue(
            Stmt.newObject(parameterizedAs(BindableProxyAgent.class, typeParametersOf(bindable)),
                Variable.get("this"), Variable.get("target"), Variable.get("initialState"))))
        .append(generatePropertiesMap())
        .finish()
        .publicMethod(BindableProxyAgent.class, "getProxyAgent")
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

    generateAccessorMethods(classBuilder);
    generateNonAccessorMethods(classBuilder);

    return classBuilder;
  }

  /**
   * Generates accessor methods for all Java bean properties plus the corresponding code for the method implementations
   * of {@link HasProperties}.
   */
  private void generateAccessorMethods(ClassStructureBuilder<?> classBuilder) {
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

  /**
   * Generates a getter method for the provided property plus the corresponding code for the implementation of
   * {@link HasProperties#get(String)}.
   */
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

  /**
   * Generates a getter method for the provided property plus the corresponding code for the implementation of
   * {@link HasProperties#set(String, Object)}.
   */
  private void generateSetter(ClassStructureBuilder<?> classBuilder, String property, BlockBuilder<?> setMethod) {
    MetaMethod getterMethod = bindable.getBeanDescriptor().getReadMethodForProperty(property);
    MetaMethod setterMethod = bindable.getBeanDescriptor().getWriteMethodForProperty(property);
    if (getterMethod != null && setterMethod != null && !setterMethod.isFinal()) {
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

      // If the set method we are proxying returns a value, capture that value into a local variable
      Statement callSetterOnTarget = null;
      Statement returnValueOfSetter = null;
      if (!setterMethod.getReturnType().equals(MetaClassFactory.get(void.class))) {
        callSetterOnTarget =
            Stmt.declareFinalVariable("returnValueOfSetter", setterMethod.getReturnType(), callSetterOnTarget);
        returnValueOfSetter = Stmt.nestedCall(Refs.get("returnValueOfSetter")).returnValue();
      }
      else {
        callSetterOnTarget =
            target().invoke(setterMethod.getName(), Cast.to(paramType, Stmt.loadVariable(property)));
        returnValueOfSetter = EmptyStatement.INSTANCE;
      }

      Statement updateNestedProxy = null;
      if (paramType.isAnnotationPresent(Bindable.class)) {
        updateNestedProxy =
            Stmt.if_(Bool.expr(agent("binders").invoke("containsKey", property)))
                .append(Stmt.loadVariable(property).assignValue(Cast.to(paramType,
                    agent("binders").invoke("get", property).invoke("setModel", Variable.get(property)))))
                .append(Stmt.loadVariable("this").invoke("set", property, Variable.get(property)))
                .finish();
      }
      else {
        updateNestedProxy = EmptyStatement.INSTANCE;
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

  /**
   * Generates proxy methods overriding public non-final methods that are not also property accessor methods. The
   * purpose of this is to allow the proxies to react on model changes that happen outside the getters and setters of
   * the bean. These methods will cause a comparison of all bound properties and trigger the appropriate UI updates and
   * property change events.
   */
  private void generateNonAccessorMethods(ClassStructureBuilder<?> classBuilder) {
    for (MetaMethod method : bindable.getMethods()) {
      String methodName = method.getName();
      if (!methodName.startsWith("get") && !methodName.startsWith("set") && !methodName.startsWith("is")
          && !methodName.equals("hashCode") && !methodName.equals("equals") && !methodName.equals("toString")
          && method.isPublic() && !method.isFinal()) {

        Parameter[] parms = DefParameters.from(method).getParameters().toArray(new Parameter[0]);
        List<Statement> parmVars = new ArrayList<Statement>();
        for (int i = 0; i < parms.length; i++) {
          parmVars.add(Stmt.loadVariable(parms[i].getName()));
        }

        Statement callOnTarget = null;
        Statement returnValue = null;
        if (!method.getReturnType().equals(MetaClassFactory.get(void.class))) {
          callOnTarget = Stmt.declareFinalVariable("returnValue", method.getReturnType(),
              target().invoke(method, parmVars.toArray()));
          returnValue = Stmt.nestedCall(Refs.get("returnValue")).returnValue();
        }
        else {
          callOnTarget = target().invoke(method, parmVars.toArray());
          returnValue = EmptyStatement.INSTANCE;
        }

        classBuilder
            .publicMethod(method.getReturnType(), methodName, parms)
              .append(callOnTarget)
              .append(agent().invoke("updateWidgetsAndFireEvents"))
              .append(returnValue)
            .finish();
      }
    }
  }

  /**
   * Generates the code to collect all existing properties and their type.
   */
  private Statement generatePropertiesMap() {
    BlockStatement block = new BlockStatement();
    for (String property : bindable.getBeanDescriptor().getProperties()) {
      MetaMethod readMethod = bindable.getBeanDescriptor().getReadMethodForProperty(property);
      if (readMethod != null && !readMethod.isFinal()) {
        block.addStatement(agent("propertyTypes").invoke(
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

  private ContextualStatementBuilder agent(String field) {
    return agent().loadField(field);
  }

  private String inferAgentFieldName() {
    String fieldName = "agent";
    while(bindable.getInheritedField(fieldName) != null) {
      fieldName = "_" + fieldName;
    }
    return fieldName;
  }
  
  private ContextualStatementBuilder agent() {
    return Stmt.loadClassMember(agentField);
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