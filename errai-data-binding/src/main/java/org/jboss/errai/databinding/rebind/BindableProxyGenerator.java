/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
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
import static org.jboss.errai.codegen.util.Stmt.loadLiteral;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.jboss.errai.codegen.builder.ElseBlockBuilder;
import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.meta.MetaParameterizedType;
import org.jboss.errai.codegen.meta.MetaType;
import org.jboss.errai.codegen.meta.MetaTypeVariable;
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
import org.jboss.errai.databinding.client.api.StateSync;

import com.google.gwt.core.ext.TreeLogger;

/**
 * Generates a proxy for a {@link Bindable} type. A bindable proxy subclasses the bindable type and
 * overrides all non-final methods to trigger UI updates and fire property change events when
 * required.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class BindableProxyGenerator {
  private final MetaClass bindable;
  private final String agentField;
  private final TreeLogger logger;
  private final Set<MetaMethod> proxiedAccessorMethods;

  public BindableProxyGenerator(MetaClass bindable, TreeLogger logger) {
    this.bindable = bindable;
    this.agentField = inferSafeAgentFieldName();
    this.logger = logger;
    this.proxiedAccessorMethods = new HashSet<MetaMethod>();
  }

  public ClassStructureBuilder<?> generate() {
    String safeProxyClassName = bindable.getFullyQualifiedName().replace('.', '_') + "Proxy";
    ClassStructureBuilder<?> classBuilder = ClassBuilder.define(safeProxyClassName, bindable)
        .packageScope()
        .implementsInterface(BindableProxy.class)
        .body();

    classBuilder
        .privateField(agentField, parameterizedAs(BindableProxyAgent.class, typeParametersOf(bindable)))
        .finish()
        .publicConstructor()
        .callThis(Stmt.newObject(bindable))
        .finish()
        .publicConstructor(Parameter.of(bindable, "target"))
        .append(Stmt.loadVariable(agentField).assignValue(
            Stmt.newObject(parameterizedAs(BindableProxyAgent.class, typeParametersOf(bindable)),
                Variable.get("this"), Variable.get("target"))))
        .append(generatePropertiesMap())
        .append(agent().invoke("copyValues"))
        .finish()
        .publicMethod(BindableProxyAgent.class, "getBindableProxyAgent")
        .append(agent().returnValue())
        .finish()
        .publicMethod(void.class, "updateWidgets")
        .append(agent().invoke("updateWidgetsAndFireEvents"))
        .finish()
        .publicMethod(bindable, "unwrap")
        .append(target().returnValue())
        .finish()
        .publicMethod(bindable, "deepUnwrap")
        .append(generateDeepUnwrapMethodBody("deepUnwrap"))
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
   * Generates accessor methods for all Java bean properties plus the corresponding code for the
   * method implementations of {@link HasProperties}.
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
    getMethod.append(
        If.objEquals(Stmt.loadVariable("property"), "this")
            .append(target().returnValue())
            .finish()
        );
    setMethod.append(
        If.cond(Stmt.loadVariable("property").invoke("equals", "this"))
            .append(agent().loadField("target").assignValue(
                    Stmt.castTo(bindable, Stmt.loadVariable("value"))))
            .append(
                Stmt.returnVoid())
            .finish()
        );

    Statement nonExistingPropertyException = Stmt.throw_(NonExistingPropertyException.class, Variable.get("property"));
    getMethod.append(nonExistingPropertyException).finish();
    setMethod.append(nonExistingPropertyException).finish();

    classBuilder.publicMethod(Map.class, "getBeanProperties")
      .append(Stmt.declareFinalVariable("props", Map.class, ObjectBuilder.newInstanceOf(HashMap.class).withParameters(agent().loadField("propertyTypes"))))
      .append(Stmt.loadVariable("props").invoke("remove", "this"))
      .append(Stmt.invokeStatic(Collections.class, "unmodifiableMap", Stmt.loadVariable("props")).returnValue())
    .finish();
  }

  /**
   * Generates a getter method for the provided property plus the corresponding code for the
   * implementation of {@link HasProperties#get(String)}.
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

      proxiedAccessorMethods.add(getterMethod);
    }
  }

  /**
   * Generates a setter method for the provided property plus the corresponding code for the
   * implementation of {@link HasProperties#set(String, Object)}.
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

      // If the setter method we are proxying returns a value, capture that value into a local variable
      Statement returnValueOfSetter = null;
      String returnValName = ensureSafeLocalVariableName("returnValueOfSetter", setterMethod);

      Statement wrappedListProperty = EmptyStatement.INSTANCE;
      if (paramType.isAssignableTo(List.class)) {
        wrappedListProperty = Stmt.loadVariable(property).assignValue(
            Cast.to(paramType ,agent().invoke("ensureBoundListIsProxied", property, Stmt.loadVariable(property))));
      }

      Statement callSetterOnTarget =
          target().invoke(setterMethod.getName(), Cast.to(paramType, Stmt.loadVariable(property)));
      if (!setterMethod.getReturnType().equals(MetaClassFactory.get(void.class))) {
        callSetterOnTarget =
            Stmt.declareFinalVariable(returnValName, setterMethod.getReturnType(), callSetterOnTarget);
        returnValueOfSetter = Stmt.nestedCall(Refs.get(returnValName)).returnValue();
      }
      else {
        returnValueOfSetter = EmptyStatement.INSTANCE;
      }

      Statement updateNestedProxy = null;
      if (DataBindingUtil.isBindableType(paramType)) {
        updateNestedProxy =
            Stmt.if_(Bool.expr(agent("binders").invoke("containsKey", property)))
                .append(Stmt.loadVariable(property).assignValue(Cast.to(paramType,
                    agent("binders").invoke("get", property).invoke("setModel", Variable.get(property),
                        Stmt.loadStatic(StateSync.class, "FROM_MODEL"),
                        Stmt.loadLiteral(true)))))
                .finish();
      }
      else {
        updateNestedProxy = EmptyStatement.INSTANCE;
      }

      String oldValName = ensureSafeLocalVariableName("oldValue", setterMethod);
      final boolean propertyIsList = bindable.getBeanDescriptor().getPropertyType(property).getFullyQualifiedName().equals(List.class.getName());
      classBuilder.publicMethod(setterMethod.getReturnType(), setterMethod.getName(),
          Parameter.of(paramType, property))
          .append(updateNestedProxy)
          .append(
              Stmt.declareVariable(oldValName, paramType, target().invoke(getterMethod.getName())))
          .append(wrappedListProperty)
          .append(callSetterOnTarget)
          .append(
              agent().invoke("updateWidgetsAndFireEvent", propertyIsList, property, Variable.get(oldValName), Variable.get(property)))
          .append(returnValueOfSetter)
          .finish();

      proxiedAccessorMethods.add(setterMethod);
    }
  }

  /**
   * Generates proxy methods overriding public non-final methods that are not also property accessor
   * methods. The purpose of this is to allow the proxies to react on model changes that happen
   * outside setters of the bean. These methods will cause a comparison of all bound properties and
   * trigger the appropriate UI updates and property change events.
   */
  private void generateNonAccessorMethods(ClassStructureBuilder<?> classBuilder) {
    for (MetaMethod method : bindable.getMethods()) {
      String methodName = method.getName();
      if (!proxiedAccessorMethods.contains(method)
          && !methodName.equals("hashCode") && !methodName.equals("equals") && !methodName.equals("toString")
          && method.isPublic() && !method.isFinal() && !method.isStatic()) {

        Parameter[] parms = DefParameters.from(method).getParameters().toArray(new Parameter[0]);
        List<Statement> parmVars = new ArrayList<Statement>();
        for (int i = 0; i < parms.length; i++) {
          parmVars.add(Stmt.loadVariable(parms[i].getName()));
          MetaClass type = getTypeOrFirstUpperBound(method.getGenericParameterTypes()[i], method);
          if (type == null) return;
          parms[i] = Parameter.of(type, parms[i].getName());
        }

        Statement callOnTarget = null;
        Statement returnValue = null;
        String returnValName = ensureSafeLocalVariableName("returnValue", method);

        MetaClass returnType = getTypeOrFirstUpperBound(method.getGenericReturnType(), method);
        if (returnType == null)
          return;

        if (!returnType.equals(MetaClassFactory.get(void.class))) {
          callOnTarget = Stmt.declareFinalVariable(returnValName,
              returnType, target().invoke(method, parmVars.toArray()));
          returnValue = Stmt.nestedCall(Refs.get(returnValName)).returnValue();
        }
        else {
          callOnTarget = target().invoke(method, parmVars.toArray());
          returnValue = EmptyStatement.INSTANCE;
        }

        classBuilder
            .publicMethod(returnType, methodName, parms)
              .append(callOnTarget)
              .append(agent().invoke("updateWidgetsAndFireEvents"))
              .append(returnValue)
            .finish();
      }
    }
  }

  /**
   * Generates code to collect all existing properties and their types.
   */
  private Statement generatePropertiesMap() {
    BlockStatement block = new BlockStatement();
    for (String property : bindable.getBeanDescriptor().getProperties()) {
      MetaMethod readMethod = bindable.getBeanDescriptor().getReadMethodForProperty(property);
      if (readMethod != null && !readMethod.isFinal()) {
        final MetaClass propertyType = readMethod.getReturnType();
        block.addStatement(agent("propertyTypes").invoke(
            "put",
            property,
            Stmt.newObject(PropertyType.class, loadLiteral(propertyType.asBoxed()),
                DataBindingUtil.isBindableType(propertyType),
                propertyType.isAssignableTo(List.class))
            )
        );
      }
    }
    block.addStatement(agent("propertyTypes").invoke(
        "put",
        "this",
        Stmt.newObject(PropertyType.class, loadLiteral(bindable.asBoxed()),
            true,
            bindable.isAssignableTo(List.class))
        )
    );
    return (block.isEmpty()) ? EmptyStatement.INSTANCE : block;
  }

  /**
   * Generates method body for recursively unwrapping a {@link BindableProxy}.
   */
  private Statement generateDeepUnwrapMethodBody(final String methodName) {
    final String cloneVar = "clone";
    final BlockStatement block = new BlockStatement();
    block.addStatement(Stmt.declareFinalVariable(cloneVar, bindable, Stmt.newObject(bindable)));

    for (final String property : bindable.getBeanDescriptor().getProperties()) {
      final MetaMethod readMethod = bindable.getBeanDescriptor().getReadMethodForProperty(property);
      final MetaMethod writeMethod = bindable.getBeanDescriptor().getWriteMethodForProperty(property);
      if (readMethod != null && writeMethod != null) {
        MetaClass type = readMethod.getReturnType();
        if (!DataBindingUtil.isBindableType(type)) {
          // If we find a collection we copy its elements and unwrap them if necessary
          // TODO support map types
          if (type.isAssignableTo(Collection.class)) {
            String colVarName = property + "Clone";
            String elemVarName = property + "Elem";

            BlockBuilder<ElseBlockBuilder> colBlock = If.isNotNull(Stmt.nestedCall(target().invoke(readMethod)));

            if ((type.isInterface() || type.isAbstract()) &&
                    (type.isAssignableTo(List.class) || type.isAssignableTo(Set.class))) {
              MetaClass clazz = (type.isAssignableTo(Set.class))
                      ? MetaClassFactory.get(HashSet.class) : MetaClassFactory.get(ArrayList.class);
                colBlock.append(Stmt.declareFinalVariable(colVarName, type.getErased(), Stmt.newObject(clazz)));
            }
            else {
              if (!type.isInterface() && !type.isAbstract()) {
                colBlock.append(Stmt.declareFinalVariable(colVarName, type.getErased(), Stmt.newObject(type.getErased())));
              }
              else {
                logger.log(TreeLogger.WARN, "Bean validation on collection " + property + " in class " + bindable +
                        " won't work. Change to either List or Set or use a concrete type instead.");
                continue;
              }
            }
            // Check if the collection element is proxied and unwrap if necessary
            colBlock.append(
              Stmt.nestedCall(target().invoke(readMethod)).foreach(elemVarName, Object.class)
               .append (
                 If.instanceOf(Refs.get(elemVarName), BindableProxy.class)
                  .append (Stmt.loadVariable(colVarName)
                    .invoke("add", Stmt.castTo(BindableProxy.class, Stmt.loadVariable(elemVarName)).invoke(methodName))
                  )
               .finish()
               .else_()
                 .append(Stmt.loadVariable(colVarName).invoke("add", Refs.get(elemVarName)))
               .finish()
             )
             .finish());

            colBlock.append(Stmt.loadVariable(cloneVar).invoke(writeMethod, Refs.get(colVarName)));
            block.addStatement(colBlock.finish());
          }
          else {
            block.addStatement(Stmt.loadVariable(cloneVar).invoke(writeMethod,target().invoke(readMethod)));
          }
        }
        // Found a bindable property: Generate code to unwrap for the case the instance is proxied
        else {
          final Statement field = target().invoke(readMethod);
          block.addStatement (
            If.instanceOf(field, BindableProxy.class)
              .append(Stmt.loadVariable(cloneVar).invoke(writeMethod,
                        Cast.to (
                            readMethod.getReturnType(),
                            Stmt.castTo(BindableProxy.class, Stmt.loadVariable("this").invoke(readMethod))
                            .invoke(methodName)
                        )
                    )
                )
              .finish()
              .else_()
                .append(Stmt.loadVariable(cloneVar).invoke(writeMethod, target().invoke(readMethod)))
              .finish()
          );
        }
      }
    }

    block.addStatement(Stmt.loadVariable(cloneVar).returnValue());

    return block;
  }

  private String inferSafeAgentFieldName() {
    String fieldName = "agent";
    while (bindable.getInheritedField(fieldName) != null) {
      fieldName = "_" + fieldName;
    }
    return fieldName;
  }

  private String ensureSafeLocalVariableName(String name, MetaMethod method) {
    MetaParameter[] params = method.getParameters();
    if (params != null) {
      for (MetaParameter param : params) {
        if (name.equals(param.getName())) {
          name = "_" + name;
          break;
        }
      }
    }
    return name;
  }

  private ContextualStatementBuilder agent(String field) {
    return agent().loadField(field);
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

  private MetaClass getTypeOrFirstUpperBound(MetaType clazz, MetaMethod method) {
    if (clazz instanceof MetaTypeVariable) {
      MetaType[] bounds = ((MetaTypeVariable) clazz).getBounds();
      if (bounds.length == 1 && bounds[0] instanceof MetaClass) {
        clazz = ((MetaTypeVariable) clazz).getBounds()[0];
      }
      else {
        // TODO add full support for generics in errai codegen
        logger.log(TreeLogger.WARN, "Ignoring method: " + method + " in class " + bindable + ". Methods using " +
            "multiple type parameters or type parameters with multiple bounds are currently not supported in " +
            "@Bindable types! Invoking this method on a bound model will have unpredictable results.");
        return null;
      }
    }
    else if (clazz instanceof MetaParameterizedType) {
      clazz = ((MetaParameterizedType) clazz).getRawType();
    }
    if (clazz instanceof MetaClass) {
      return (MetaClass) clazz;
    }

    logger.log(TreeLogger.WARN, "Ignoring method: " + method + " in class " + bindable + ". Method cannot be proxied!");
    return null;
  }
}
