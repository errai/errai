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

package org.jboss.errai.codegen.framework.builder.impl;

import org.jboss.errai.codegen.framework.Context;
import org.jboss.errai.codegen.framework.DefModifiers;
import org.jboss.errai.codegen.framework.DefParameters;
import org.jboss.errai.codegen.framework.Parameter;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.ThrowsDeclaration;
import org.jboss.errai.codegen.framework.builder.BuildCallback;
import org.jboss.errai.codegen.framework.builder.ClassDefinitionBuilderAbstractOption;
import org.jboss.errai.codegen.framework.builder.ClassDefinitionBuilderInterfaces;
import org.jboss.errai.codegen.framework.builder.ClassDefinitionBuilderScope;
import org.jboss.errai.codegen.framework.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.framework.builder.ConstructorBlockBuilder;
import org.jboss.errai.codegen.framework.builder.DefaultClassStructureBuilder;
import org.jboss.errai.codegen.framework.builder.FieldBuildInitializer;
import org.jboss.errai.codegen.framework.builder.MethodBlockBuilder;
import org.jboss.errai.codegen.framework.builder.MethodBuildCallback;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.meta.impl.build.BuildMetaClass;
import org.jboss.errai.codegen.framework.meta.impl.build.BuildMetaConstructor;
import org.jboss.errai.codegen.framework.meta.impl.build.BuildMetaField;
import org.jboss.errai.codegen.framework.meta.impl.build.BuildMetaMethod;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ClassBuilder<T extends ClassStructureBuilder<T>> implements
        ClassDefinitionBuilderScope<T>,
        ClassDefinitionBuilderAbstractOption<T>,
        ClassStructureBuilder<T> {

  protected BuildMetaClass classDefinition;

  ClassBuilder(String className, MetaClass parent, Context context) {
    this.classDefinition = new BuildMetaClass(context);
    this.classDefinition.setClassName(className);
    this.classDefinition.setSuperClass(parent);
    context.attachClass(classDefinition);
  }

  ClassBuilder(ClassBuilder<T> that, Context context) {
    this.classDefinition = that.classDefinition;
    this.classDefinition.setContext(context);
    context.attachClass(classDefinition);
  }

  public static ClassDefinitionBuilderScope<?> define(String fullyQualifiedName) {
    return new ClassBuilder<DefaultClassStructureBuilder>(fullyQualifiedName, null, Context.create().autoImport());
  }

  public static ClassDefinitionBuilderScope<?> define(String fullQualifiedName, MetaClass parent) {
    return new ClassBuilder<DefaultClassStructureBuilder>(fullQualifiedName, parent, Context.create().autoImport());
  }

  public static ClassDefinitionBuilderScope<?> define(String fullQualifiedName, Class<?> parent) {
    return define(fullQualifiedName, MetaClassFactory.get(parent));
  }

  public static ClassStructureBuilder<?> implement(MetaClass cls) {
    return new ClassBuilder<DefaultClassStructureBuilder>(cls.getFullyQualifiedName() + "Impl", null, Context.create()
        .autoImport())
        .publicScope()
        .implementsInterface(cls).body();
  }

  public static ClassStructureBuilder<?> implement(Class<?> cls) {
    return implement(MetaClassFactory.get(cls));
  }

  @Override
  public ClassBuilderAbstractMethodOption abstractClass() {
    classDefinition.setAbstract(true);
    return new ClassBuilderAbstractMethodOption(this, classDefinition.getContext());
  }

  public ClassBuilder<T> importsClass(Class<?> clazz) {
    return importsClass(MetaClassFactory.get(clazz));
  }

  public ClassBuilder<T> importsClass(MetaClass clazz) {
    classDefinition.getContext().addClassImport(clazz);
    return this;
  }

  @Override
  public ClassDefinitionBuilderInterfaces<T> implementsInterface(Class<?> clazz) {
    return implementsInterface(MetaClassFactory.get(clazz));
  }

  @Override
  public ClassDefinitionBuilderInterfaces<T> implementsInterface(MetaClass clazz) {
    if (!clazz.isInterface()) {
      throw new RuntimeException("not an interface: " + clazz.getFullyQualifiedName());
    }

    classDefinition.addInterface(clazz);
    return this;
  }

  @Override
  public ClassStructureBuilder<T> body() {
    return this;
  }

  @Override
  public ClassDefinitionBuilderAbstractOption<T> publicScope() {
    classDefinition.setScope(Scope.Public);
    return this;
  }

  @Override
  public ClassDefinitionBuilderAbstractOption<T> privateScope() {
    classDefinition.setScope(Scope.Private);
    return this;
  }

  @Override
  public ClassDefinitionBuilderAbstractOption<T> protectedScope() {
    classDefinition.setScope(Scope.Protected);
    return this;
  }

  @Override
  public ClassDefinitionBuilderAbstractOption<T> packageScope() {
    classDefinition.setScope(Scope.Package);
    return this;
  }

  @Override
  public ConstructorBlockBuilder<T> publicConstructor() {
    return genConstructor(Scope.Public, DefParameters.none());
  }
  
  @Override
  public ConstructorBlockBuilder<T> publicConstructor(MetaClass... parms) {
    return genConstructor(Scope.Public, DefParameters.fromTypeArray(parms));
  }

  @Override
  public ConstructorBlockBuilder<T> publicConstructor(Class<?>... parms) {
    return publicConstructor(MetaClassFactory.fromClassArray(parms));
  }

  @Override
  public ConstructorBlockBuilder<T> publicConstructor(Parameter... parms) {
    return genConstructor(Scope.Public, DefParameters.fromParameters(parms));
  }

  @Override
  public ConstructorBlockBuilder<T> privateConstructor() {
    return genConstructor(Scope.Private, DefParameters.none());
  }
  
  @Override
  public ConstructorBlockBuilder<T> privateConstructor(MetaClass... parms) {
    return genConstructor(Scope.Private, DefParameters.fromTypeArray(parms));
  }

  @Override
  public ConstructorBlockBuilder<T> privateConstructor(Class<?>... parms) {
    return privateConstructor(MetaClassFactory.fromClassArray(parms));
  }

  @Override
  public ConstructorBlockBuilder<T> privateConstructor(Parameter... parms) {
    return genConstructor(Scope.Private, DefParameters.fromParameters(parms));
  }

  @Override
  public ConstructorBlockBuilder<T> protectedConstructor() {
    return genConstructor(Scope.Protected, DefParameters.none());
  }
  
  @Override
  public ConstructorBlockBuilder<T> protectedConstructor(MetaClass... parms) {
    return genConstructor(Scope.Protected, DefParameters.fromTypeArray(parms));
  }

  @Override
  public ConstructorBlockBuilder<T> protectedConstructor(Class<?>... parms) {
    return protectedConstructor(MetaClassFactory.fromClassArray(parms));
  }

  @Override
  public ConstructorBlockBuilder<T> protectedConstructor(Parameter... parms) {
    return genConstructor(Scope.Protected, DefParameters.fromParameters(parms));
  }

  @Override
  public ConstructorBlockBuilder<T> packageConstructor() {
    return genConstructor(Scope.Package, DefParameters.none());
  }
  
  @Override
  public ConstructorBlockBuilder<T> packageConstructor(MetaClass... parms) {
    return genConstructor(Scope.Package, DefParameters.fromTypeArray(parms));
  }

  @Override
  public ConstructorBlockBuilder<T> packageConstructor(Class<?>... parms) {
    return packageConstructor(MetaClassFactory.fromClassArray(parms));
  }

  @Override
  public ConstructorBlockBuilder<T> packageConstructor(Parameter... parms) {
    return genConstructor(Scope.Package, DefParameters.fromParameters(parms));
  }

  private ConstructorBlockBuilder<T> genConstructor(final Scope scope, final DefParameters
          defParameters) {
    return new ConstructorBlockBuilderImpl<T>(new BuildCallback<T>() {

      @Override
      public T callback(final Statement statement) {
        BuildMetaConstructor buildMetaConstructor =
                new BuildMetaConstructor(classDefinition, statement, defParameters);
        buildMetaConstructor.setScope(scope);

        classDefinition.addConstructor(buildMetaConstructor);
        return (T) ClassBuilder.this;
      }
    });
  }

  // public method //
  @Override
  public MethodBlockBuilder<T> publicMethod(MetaClass returnType, String name) {
    return genMethod(Scope.Public, returnType, name, DefParameters.none());
  }

  @Override
  public MethodBlockBuilder<T> publicMethod(Class<?> returnType, String name) {
    return publicMethod(MetaClassFactory.get(returnType), name);
  }
    
  @Override
  public MethodBlockBuilder<T> publicMethod(MetaClass returnType, String name, MetaClass... parms) {
    return genMethod(Scope.Public, returnType, name, DefParameters.fromTypeArray(parms));
  }

  @Override
  public MethodBlockBuilder<T> publicMethod(Class<?> returnType, String name, Class<?>... parms) {
    return publicMethod(MetaClassFactory.get(returnType), name, MetaClassFactory.fromClassArray(parms));
  }

  @Override
  public MethodBlockBuilder<T> publicMethod(MetaClass returnType, String name, Parameter... parms) {
    return genMethod(Scope.Public, returnType, name, DefParameters.fromParameters(parms));
  }

  @Override
  public MethodBlockBuilder<T> publicMethod(Class<?> returnType, String name, Parameter... parms) {
    return publicMethod(MetaClassFactory.get(returnType), name, parms);
  }

  // private method //
  @Override
  public MethodBlockBuilder<T> privateMethod(MetaClass returnType, String name) {
    return genMethod(Scope.Private, returnType, name, DefParameters.none());
  }

  @Override
  public MethodBlockBuilder<T> privateMethod(Class<?> returnType, String name) {
    return privateMethod(MetaClassFactory.get(returnType), name);
  }

  @Override
  public MethodBlockBuilder<T> privateMethod(MetaClass returnType, String name, MetaClass... parms) {
    return genMethod(Scope.Private, returnType, name, DefParameters.fromTypeArray(parms));
  }

  @Override
  public MethodBlockBuilder<T> privateMethod(Class<?> returnType, String name, Class<?>... parms) {
    return privateMethod(MetaClassFactory.get(returnType), name, MetaClassFactory.fromClassArray(parms));
  }

  @Override
  public MethodBlockBuilder<T> privateMethod(MetaClass returnType, String name, Parameter... parms) {
    return genMethod(Scope.Private, returnType, name, DefParameters.fromParameters(parms));
  }

  @Override
  public MethodBlockBuilder<T> privateMethod(Class<?> returnType, String name, Parameter... parms) {
    return privateMethod(MetaClassFactory.get(returnType), name, parms);
  }

  // protected method //
  @Override
  public MethodBlockBuilder<T> protectedMethod(MetaClass returnType, String name) {
    return genMethod(Scope.Protected, returnType, name, DefParameters.none());
  }

  @Override
  public MethodBlockBuilder<T> protectedMethod(Class<?> returnType, String name) {
    return protectedMethod(MetaClassFactory.get(returnType), name);
  }
  
  @Override
  public MethodBlockBuilder<T> protectedMethod(MetaClass returnType, String name, MetaClass... parms) {
    return genMethod(Scope.Protected, returnType, name, DefParameters.fromTypeArray(parms));
  }

  @Override
  public MethodBlockBuilder<T> protectedMethod(Class<?> returnType, String name, Class<?>... parms) {
    return protectedMethod(MetaClassFactory.get(returnType), name, MetaClassFactory.fromClassArray(parms));
  }

  @Override
  public MethodBlockBuilder<T> protectedMethod(MetaClass returnType, String name, Parameter... parms) {
    return genMethod(Scope.Protected, returnType, name, DefParameters.fromParameters(parms));
  }

  @Override
  public MethodBlockBuilder<T> protectedMethod(Class<?> returnType, String name, Parameter... parms) {
    return protectedMethod(MetaClassFactory.get(returnType), name, parms);
  }

  // package-private method //
  @Override
  public MethodBlockBuilder<T> packageMethod(MetaClass returnType, String name) {
    return genMethod(Scope.Package, returnType, name, DefParameters.none());
  }

  @Override
  public MethodBlockBuilder<T> packageMethod(Class<?> returnType, String name) {
    return packageMethod(MetaClassFactory.get(returnType), name);
  }

  @Override
  public MethodBlockBuilder<T> packageMethod(MetaClass returnType, String name, MetaClass... parms) {
    return genMethod(Scope.Package, returnType, name, DefParameters.fromTypeArray(parms));
  }

  @Override
  public MethodBlockBuilder<T> packageMethod(Class<?> returnType, String name, Class<?>... parms) {
    return packageMethod(MetaClassFactory.get(returnType), name, MetaClassFactory.fromClassArray(parms));
  }

  @Override
  public MethodBlockBuilder<T> packageMethod(MetaClass returnType, String name, Parameter... parms) {
    return genMethod(Scope.Package, returnType, name, DefParameters.fromParameters(parms));
  }

  @Override
  public MethodBlockBuilder<T> packageMethod(Class<?> returnType, String name, Parameter... parms) {
    return packageMethod(MetaClassFactory.get(returnType), name, parms);
  }

  private MethodBlockBuilder<T> genMethod(final Scope scope,
                                          final MetaClass returnType,
                                          final String name,
                                          final DefParameters defParameters) {

    return new MethodBlockBuilderImpl<T>(new MethodBuildCallback<T>() {
      @Override
      public T callback(final Statement statement,
                        final DefParameters parameters,
                        final DefModifiers modifiers,
                        final ThrowsDeclaration throwsDeclaration) {

        DefParameters dParameters;

        if (parameters != null) {
          dParameters = parameters;
        }
        else {
          dParameters = defParameters;          
        }

        BuildMetaMethod buildMetaMethod = new BuildMetaMethod(classDefinition, statement, scope,
                modifiers, name, returnType, null, dParameters, throwsDeclaration);
 
        classDefinition.addMethod(buildMetaMethod);
        return (T) ClassBuilder.this;
      }
    });
  }

  @Override
  public FieldBuildInitializer<T> publicField(String name, MetaClass type) {
    return genField(Scope.Public, name, type);
  }

  @Override
  public FieldBuildInitializer<T> publicField(String name, Class<?> type) {
    return publicField(name, MetaClassFactory.get(type));
  }

  @Override
  public FieldBuildInitializer<T> privateField(String name, MetaClass type) {
    return genField(Scope.Private, name, type);
  }

  @Override
  public FieldBuildInitializer<T> privateField(String name, Class<?> type) {
    return privateField(name, MetaClassFactory.get(type));
  }

  @Override
  public FieldBuildInitializer<T> protectedField(String name, MetaClass type) {
    return genField(Scope.Protected, name, type);
  }

  @Override
  public FieldBuildInitializer<T> protectedField(String name, Class<?> type) {
    return protectedField(name, MetaClassFactory.get(type));
  }

  @Override
  public FieldBuildInitializer<T> packageField(String name, MetaClass type) {
    return genField(Scope.Package, name, type);
  }

  @Override
  public FieldBuildInitializer<T> packageField(String name, Class<?> type) {
    return packageField(name, MetaClassFactory.get(type));
  }

  private FieldBuildInitializer<T> genField(final Scope scope, final String name,
                                                                    final MetaClass type) {
    return new FieldBuilder<T>(new BuildCallback<T>() {
      @Override
      public T callback(final Statement statement) {
        BuildMetaField buildMetaField
                = new BuildMetaField(classDefinition, statement, scope, type, name);

        classDefinition.addField(buildMetaField);
        return (T) ClassBuilder.this;
      }
    }, scope, type, name);
  }

  public MetaClass getClassDefinition() {
    return classDefinition;
  }

  @Override
  public String toJavaString() {
    return classDefinition.toJavaString();
  }
}