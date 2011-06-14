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

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.*;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class ClassBuilder extends AbstractStatementBuilder implements
        ClassDefinitionBuilderScope,
        ClassDefinitionBuilderAbstractOption,
        BaseClassStructureBuilder<BaseClassStructureBuilder> {
  private String name;
  private Scope scope;
  private MetaClass parent;
  private Set<MetaClass> interfaces = new HashSet<MetaClass>();
  private boolean isAbstract;

  ClassBuilder(String name, MetaClass parent, Context context, CallElementBuilder callElementBuilder) {
    super(context, callElementBuilder);
    this.name = name;
    this.parent = parent;
  }

  ClassBuilder(String name, MetaClass parent, Context context) {
    super(context);
    this.name = name;
    this.parent = parent;
  }

  public static ClassBuilder define(String fullyQualifiedName) {
    return new ClassBuilder(fullyQualifiedName, null, Context.create());
  }

  public static ClassBuilder define(String fullQualifiedName, MetaClass parent) {
    return new ClassBuilder(fullQualifiedName, parent, Context.create());
  }

  public ClassBuilder abstractClass() {
    isAbstract = true;
    return this;
  }

  public ClassBuilder importsClass(Class<?> clazz) {
    return importsClass(MetaClassFactory.get(clazz));
  }

  public ClassBuilder importsClass(MetaClass clazz) {
    context.addClassImport(clazz);
    return this;
  }

  public ClassDefinitionBuilderInterfaces implementsInterface(Class<?> clazz) {
    return implementsInterface(MetaClassFactory.get(clazz));
  }

  public ClassDefinitionBuilderInterfaces implementsInterface(MetaClass clazz) {
    interfaces.add(clazz);
    return this;
  }

  public BaseClassStructureBuilder<BaseClassStructureBuilder> body() {
    return null;
  }

  public ClassDefinitionBuilderAbstractOption publicScope() {
    scope = Scope.Public;
    return this;
  }

  public ClassDefinitionBuilderAbstractOption privateScope() {
    scope = Scope.Private;
    return this;
  }

  public ClassDefinitionBuilderAbstractOption protectedScope() {
    scope = Scope.Protected;
    return this;
  }

  public ClassDefinitionBuilderAbstractOption packageScope() {
    scope = Scope.Package;
    return this;
  }

  public BlockBuilder<BaseClassStructureBuilder> publicConstructor(MetaClass... parms) {
    return null;
  }

  public BlockBuilder<BaseClassStructureBuilder> publicConstructor(Class<?>... parms) {
    return null;
  }

  public BlockBuilder<BaseClassStructureBuilder> privateConstructor(MetaClass... parms) {
    return null;
  }

  public BlockBuilder<BaseClassStructureBuilder> privateConstructor(Class<?>... parms) {
    return null;
  }

  public BlockBuilder<BaseClassStructureBuilder> protectedConstructor(MetaClass... parms) {
    return null;
  }

  public BlockBuilder<BaseClassStructureBuilder> protectedConstructor(Class<?>... parms) {
    return null;
  }

  public BlockBuilder<BaseClassStructureBuilder> packageConstructor(MetaClass... parms) {
    return null;
  }

  public BlockBuilder<BaseClassStructureBuilder> packageConstructor(Class<?>... parms) {
    return null;
  }

  public BlockBuilder<BaseClassStructureBuilder> publicMethod(MetaClass returnType, String name, MetaClass... parms) {
    return null;
  }

  public BlockBuilder<BaseClassStructureBuilder> publicMethod(Class<?> returnType, String name, Class<?>... parms) {
    return null;
  }

  public BlockBuilder<BaseClassStructureBuilder> privateMethod(MetaClass returnType, String name, MetaClass... parms) {
    return null;
  }

  public BlockBuilder<BaseClassStructureBuilder> privateMethod(Class<?> returnType, String name, Class<?>... parms) {
    return null;
  }

  public BlockBuilder<BaseClassStructureBuilder> protectedMethod(MetaClass returnType, String name, MetaClass... parms) {
    return null;
  }

  public BlockBuilder<BaseClassStructureBuilder> protectedMethod(Class<?> returnType, String name, Class<?>... parms) {
    return null;
  }

  public BlockBuilder<BaseClassStructureBuilder> packageMethod(MetaClass returnType, String name, MetaClass... parms) {
    return null;
  }

  public BlockBuilder<BaseClassStructureBuilder> packageMethod(Class<?> returnType, String name, Class<?>... parms) {
    return null;
  }

  public FieldBuilder<BaseClassStructureBuilder> publicField(String name, MetaClass type) {
    return null;
  }

  public FieldBuilder<BaseClassStructureBuilder> publicField(String name, Class<?> type) {
    return null;
  }

  public FieldBuilder<BaseClassStructureBuilder> privateField(String name, MetaClass type) {
    return null;
  }

  public FieldBuilder<BaseClassStructureBuilder> privateField(String name, Class<?> type) {
    return null;
  }

  public FieldBuilder<BaseClassStructureBuilder> protectedField(String name, MetaClass type) {
    return null;
  }

  public FieldBuilder<BaseClassStructureBuilder> protectedField(String name, Class<?> type) {
    return null;
  }

  public FieldBuilder<BaseClassStructureBuilder> packageField(String name, MetaClass type) {
    return null;
  }

  public FieldBuilder<BaseClassStructureBuilder> packageField(String name, Class<?> type) {
    return null;
  }
}
