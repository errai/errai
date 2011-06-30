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

package org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl;

import org.jboss.errai.ioc.rebind.ioc.codegen.*;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.*;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.build.BuildMetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.build.BuildMetaConstructor;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.build.BuildMetaField;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.build.BuildMetaMethod;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ClassBuilder implements
        ClassDefinitionBuilderScope,
        ClassDefinitionBuilderAbstractOption,
        BaseClassStructureBuilder {

  private BuildMetaClass classDefinition;

  ClassBuilder(String className, MetaClass parent, Context context) {
    this.classDefinition = new BuildMetaClass(context);
    this.classDefinition.setClassName(className);
    this.classDefinition.setSuperClass(parent);
  }

  ClassBuilder(ClassBuilder that, Context context) {
    this.classDefinition = that.classDefinition;
    this.classDefinition.setContext(context);
  }

  public static ClassDefinitionBuilderScope define(String fullyQualifiedName) {
    return new ClassBuilder(fullyQualifiedName, null, Context.create().autoImport());
  }

  public static ClassDefinitionBuilderScope define(String fullQualifiedName, MetaClass parent) {
    return new ClassBuilder(fullQualifiedName, parent, Context.create().autoImport());
  }

  public static ClassDefinitionBuilderScope define(String fullQualifiedName, Class<?> parent) {
    return define(fullQualifiedName, MetaClassFactory.get(parent));
  }

  public static BaseClassStructureBuilder implement(MetaClass cls) {
    return new ClassBuilder(cls.getFullyQualifiedName() + "Impl", null, Context.create().autoImport())
            .publicScope()
            .implementsInterface(cls).body();
  }

  public static BaseClassStructureBuilder implement(Class<?> cls) {
    return implement(MetaClassFactory.get(cls));
  }

  @Override
  public ClassBuilder abstractClass() {
    classDefinition.setAbstract(true);
    return this;
  }

  public ClassBuilder importsClass(Class<?> clazz) {
    return importsClass(MetaClassFactory.get(clazz));
  }

  public ClassBuilder importsClass(MetaClass clazz) {
    classDefinition.getContext().addClassImport(clazz);
    return this;
  }

  @Override
  public ClassDefinitionBuilderInterfaces implementsInterface(Class<?> clazz) {
    return implementsInterface(MetaClassFactory.get(clazz));
  }

  @Override
  public ClassDefinitionBuilderInterfaces implementsInterface(MetaClass clazz) {
    if (!clazz.isInterface()) {
      throw new RuntimeException("not an interface: " + clazz.getFullyQualifiedName());
    }

    classDefinition.addInterface(clazz);
    return this;
  }

  @Override
  public BaseClassStructureBuilder body() {
    return this;
  }

  @Override
  public ClassDefinitionBuilderAbstractOption publicScope() {
    classDefinition.setScope(Scope.Public);
    return this;
  }

  @Override
  public ClassDefinitionBuilderAbstractOption privateScope() {
    classDefinition.setScope(Scope.Private);
    return this;
  }

  @Override
  public ClassDefinitionBuilderAbstractOption protectedScope() {
    classDefinition.setScope(Scope.Protected);
    return this;
  }

  @Override
  public ClassDefinitionBuilderAbstractOption packageScope() {
    classDefinition.setScope(Scope.Package);
    return this;
  }

  @Override
  public ConstructorBlockBuilder<BaseClassStructureBuilder> publicConstructor() {
    return genConstructor(Scope.Public, DefParameters.none());
  }
  
  @Override
  public ConstructorBlockBuilder<BaseClassStructureBuilder> publicConstructor(MetaClass... parms) {
    return genConstructor(Scope.Public, DefParameters.fromTypeArray(parms));
  }

  @Override
  public ConstructorBlockBuilder<BaseClassStructureBuilder> publicConstructor(Class<?>... parms) {
    return publicConstructor(MetaClassFactory.fromClassArray(parms));
  }

  @Override
  public ConstructorBlockBuilder<BaseClassStructureBuilder> publicConstructor(Parameter... parms) {
    return genConstructor(Scope.Public, DefParameters.fromParameters(parms));
  }

  @Override
  public ConstructorBlockBuilder<BaseClassStructureBuilder> privateConstructor() {
    return genConstructor(Scope.Private, DefParameters.none());
  }
  
  @Override
  public ConstructorBlockBuilder<BaseClassStructureBuilder> privateConstructor(MetaClass... parms) {
    return genConstructor(Scope.Private, DefParameters.fromTypeArray(parms));
  }

  @Override
  public ConstructorBlockBuilder<BaseClassStructureBuilder> privateConstructor(Class<?>... parms) {
    return privateConstructor(MetaClassFactory.fromClassArray(parms));
  }

  @Override
  public ConstructorBlockBuilder<BaseClassStructureBuilder> privateConstructor(Parameter... parms) {
    return genConstructor(Scope.Private, DefParameters.fromParameters(parms));
  }

  @Override
  public ConstructorBlockBuilder<BaseClassStructureBuilder> protectedConstructor() {
    return genConstructor(Scope.Protected, DefParameters.none());
  }
  
  @Override
  public ConstructorBlockBuilder<BaseClassStructureBuilder> protectedConstructor(MetaClass... parms) {
    return genConstructor(Scope.Protected, DefParameters.fromTypeArray(parms));
  }

  @Override
  public ConstructorBlockBuilder<BaseClassStructureBuilder> protectedConstructor(Class<?>... parms) {
    return protectedConstructor(MetaClassFactory.fromClassArray(parms));
  }

  @Override
  public ConstructorBlockBuilder<BaseClassStructureBuilder> protectedConstructor(Parameter... parms) {
    return genConstructor(Scope.Protected, DefParameters.fromParameters(parms));
  }

  @Override
  public ConstructorBlockBuilder<BaseClassStructureBuilder> packageConstructor() {
    return genConstructor(Scope.Package, DefParameters.none());
  }
  
  @Override
  public ConstructorBlockBuilder<BaseClassStructureBuilder> packageConstructor(MetaClass... parms) {
    return genConstructor(Scope.Package, DefParameters.fromTypeArray(parms));
  }

  @Override
  public ConstructorBlockBuilder<BaseClassStructureBuilder> packageConstructor(Class<?>... parms) {
    return packageConstructor(MetaClassFactory.fromClassArray(parms));
  }

  @Override
  public ConstructorBlockBuilder<BaseClassStructureBuilder> packageConstructor(Parameter... parms) {
    return genConstructor(Scope.Package, DefParameters.fromParameters(parms));
  }

  private ConstructorBlockBuilder<BaseClassStructureBuilder> genConstructor(final Scope scope, final DefParameters
          defParameters) {

    return new ConstructorBlockBuilderImpl<BaseClassStructureBuilder>(new BuildCallback<BaseClassStructureBuilder>() {

      @Override
      public BaseClassStructureBuilder callback(final Statement statement) {
        BuildMetaConstructor buildMetaConstructor =
                new BuildMetaConstructor(classDefinition, statement, defParameters);
        buildMetaConstructor.setScope(scope);

        classDefinition.addConstructor(buildMetaConstructor);

        return ClassBuilder.this;
      }
    });
  }

  // public method //
  @Override
  public MethodBlockBuilder<BaseClassStructureBuilder> publicMethod(MetaClass returnType, String name) {
    return genMethod(Scope.Public, returnType, name, DefParameters.none());
  }

  @Override
  public MethodBlockBuilder<BaseClassStructureBuilder> publicMethod(Class<?> returnType, String name) {
    return publicMethod(MetaClassFactory.get(returnType), name);
  }
    
  @Override
  public MethodBlockBuilder<BaseClassStructureBuilder> publicMethod(MetaClass returnType, String name, MetaClass... parms) {
    return genMethod(Scope.Public, returnType, name, DefParameters.fromTypeArray(parms));
  }

  @Override
  public MethodBlockBuilder<BaseClassStructureBuilder> publicMethod(Class<?> returnType, String name, Class<?>... parms) {
    return publicMethod(MetaClassFactory.get(returnType), name, MetaClassFactory.fromClassArray(parms));
  }

  @Override
  public MethodBlockBuilder<BaseClassStructureBuilder> publicMethod(MetaClass returnType, String name, Parameter... parms) {
    return genMethod(Scope.Public, returnType, name, DefParameters.fromParameters(parms));
  }

  @Override
  public MethodBlockBuilder<BaseClassStructureBuilder> publicMethod(Class<?> returnType, String name, Parameter... parms) {
    return publicMethod(MetaClassFactory.get(returnType), name, parms);
  }

  // private method //
  @Override
  public MethodBlockBuilder<BaseClassStructureBuilder> privateMethod(MetaClass returnType, String name) {
    return genMethod(Scope.Private, returnType, name, DefParameters.none());
  }

  @Override
  public MethodBlockBuilder<BaseClassStructureBuilder> privateMethod(Class<?> returnType, String name) {
    return privateMethod(MetaClassFactory.get(returnType), name);
  }

  @Override
  public MethodBlockBuilder<BaseClassStructureBuilder> privateMethod(MetaClass returnType, String name, MetaClass... parms) {
    return genMethod(Scope.Private, returnType, name, DefParameters.fromTypeArray(parms));
  }

  @Override
  public MethodBlockBuilder<BaseClassStructureBuilder> privateMethod(Class<?> returnType, String name, Class<?>... parms) {
    return privateMethod(MetaClassFactory.get(returnType), name, MetaClassFactory.fromClassArray(parms));
  }

  @Override
  public MethodBlockBuilder<BaseClassStructureBuilder> privateMethod(MetaClass returnType, String name, Parameter... parms) {
    return genMethod(Scope.Private, returnType, name, DefParameters.fromParameters(parms));
  }

  @Override
  public MethodBlockBuilder<BaseClassStructureBuilder> privateMethod(Class<?> returnType, String name, Parameter... parms) {
    return privateMethod(MetaClassFactory.get(returnType), name, parms);
  }

  // protected method //
  @Override
  public MethodBlockBuilder<BaseClassStructureBuilder> protectedMethod(MetaClass returnType, String name) {
    return genMethod(Scope.Protected, returnType, name, DefParameters.none());
  }

  @Override
  public MethodBlockBuilder<BaseClassStructureBuilder> protectedMethod(Class<?> returnType, String name) {
    return protectedMethod(MetaClassFactory.get(returnType), name);
  }
  
  @Override
  public MethodBlockBuilder<BaseClassStructureBuilder> protectedMethod(MetaClass returnType, String name, MetaClass... parms) {
    return genMethod(Scope.Protected, returnType, name, DefParameters.fromTypeArray(parms));
  }

  @Override
  public MethodBlockBuilder<BaseClassStructureBuilder> protectedMethod(Class<?> returnType, String name, Class<?>... parms) {
    return protectedMethod(MetaClassFactory.get(returnType), name, MetaClassFactory.fromClassArray(parms));
  }

  @Override
  public MethodBlockBuilder<BaseClassStructureBuilder> protectedMethod(MetaClass returnType, String name, Parameter... parms) {
    return genMethod(Scope.Protected, returnType, name, DefParameters.fromParameters(parms));
  }

  @Override
  public MethodBlockBuilder<BaseClassStructureBuilder> protectedMethod(Class<?> returnType, String name, Parameter... parms) {
    return protectedMethod(MetaClassFactory.get(returnType), name, parms);
  }

  // package-private method //
  @Override
  public MethodBlockBuilder<BaseClassStructureBuilder> packageMethod(MetaClass returnType, String name) {
    return genMethod(Scope.Package, returnType, name, DefParameters.none());
  }

  @Override
  public MethodBlockBuilder<BaseClassStructureBuilder> packageMethod(Class<?> returnType, String name) {
    return packageMethod(MetaClassFactory.get(returnType), name);
  }

  @Override
  public MethodBlockBuilder<BaseClassStructureBuilder> packageMethod(MetaClass returnType, String name, MetaClass... parms) {
    return genMethod(Scope.Package, returnType, name, DefParameters.fromTypeArray(parms));
  }

  @Override
  public MethodBlockBuilder<BaseClassStructureBuilder> packageMethod(Class<?> returnType, String name, Class<?>... parms) {
    return packageMethod(MetaClassFactory.get(returnType), name, MetaClassFactory.fromClassArray(parms));
  }

  @Override
  public MethodBlockBuilder<BaseClassStructureBuilder> packageMethod(MetaClass returnType, String name, Parameter... parms) {
    return genMethod(Scope.Package, returnType, name, DefParameters.fromParameters(parms));
  }

  @Override
  public MethodBlockBuilder<BaseClassStructureBuilder> packageMethod(Class<?> returnType, String name, Parameter... parms) {
    return packageMethod(MetaClassFactory.get(returnType), name, parms);
  }

  private MethodBlockBuilder<BaseClassStructureBuilder> genMethod(final Scope scope,
                                                                  final MetaClass returnType,
                                                                  final String name,
                                                                  final DefParameters defParameters) {

    return new MethodBlockBuilder<BaseClassStructureBuilder>(new MethodBuildCallback<BaseClassStructureBuilder>() {
      @Override
      public BaseClassStructureBuilder callback(final Statement statement, final ThrowsDeclaration throwsDeclaration) {
        BuildMetaMethod buildMetaMethod =
                new BuildMetaMethod(classDefinition, statement, scope, name, returnType, defParameters, throwsDeclaration);

        classDefinition.addMethod(buildMetaMethod);

        return ClassBuilder.this;
      }
    });
  }

  @Override
  public FieldBuildInitializer<BaseClassStructureBuilder> publicField(String name, MetaClass type) {
    return genField(Scope.Public, name, type);
  }

  @Override
  public FieldBuildInitializer<BaseClassStructureBuilder> publicField(String name, Class<?> type) {
    return publicField(name, MetaClassFactory.get(type));
  }

  @Override
  public FieldBuildInitializer<BaseClassStructureBuilder> privateField(String name, MetaClass type) {
    return genField(Scope.Private, name, type);
  }

  @Override
  public FieldBuildInitializer<BaseClassStructureBuilder> privateField(String name, Class<?> type) {
    return privateField(name, MetaClassFactory.get(type));
  }

  @Override
  public FieldBuildInitializer<BaseClassStructureBuilder> protectedField(String name, MetaClass type) {
    return genField(Scope.Protected, name, type);
  }

  @Override
  public FieldBuildInitializer<BaseClassStructureBuilder> protectedField(String name, Class<?> type) {
    return protectedField(name, MetaClassFactory.get(type));
  }

  @Override
  public FieldBuildInitializer<BaseClassStructureBuilder> packageField(String name, MetaClass type) {
    return genField(Scope.Package, name, type);
  }

  @Override
  public FieldBuildInitializer<BaseClassStructureBuilder> packageField(String name, Class<?> type) {
    return packageField(name, MetaClassFactory.get(type));
  }

  private FieldBuildInitializer<BaseClassStructureBuilder> genField(final Scope scope, final String name,
                                                                    final MetaClass type) {

    return new FieldBuilder<BaseClassStructureBuilder>(new BuildCallback<BaseClassStructureBuilder>() {
      @Override
      public BaseClassStructureBuilder callback(final Statement statement) {
        BuildMetaField buildMetaField
                = new BuildMetaField(classDefinition, statement, scope, type, name);

        classDefinition.addField(buildMetaField);

        return ClassBuilder.this;
      }
    }, scope, type, name);
  }

  @Override
  public String toJavaString() {
    return classDefinition.toJavaString();
  }
}