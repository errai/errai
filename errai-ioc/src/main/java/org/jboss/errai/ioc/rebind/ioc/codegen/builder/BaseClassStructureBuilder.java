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

package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.Parameter;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.BlockBuilderImpl;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.MethodBlockBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface BaseClassStructureBuilder extends Builder {
  public ConstructorBlockBuilder<BaseClassStructureBuilder> publicConstructor();
  public ConstructorBlockBuilder<BaseClassStructureBuilder> publicConstructor(MetaClass... parms);
  public ConstructorBlockBuilder<BaseClassStructureBuilder> publicConstructor(Class<?>... parms);
  public ConstructorBlockBuilder<BaseClassStructureBuilder> publicConstructor(Parameter... parms);

  public ConstructorBlockBuilder<BaseClassStructureBuilder> privateConstructor();
  public ConstructorBlockBuilder<BaseClassStructureBuilder> privateConstructor(MetaClass... parms);
  public ConstructorBlockBuilder<BaseClassStructureBuilder> privateConstructor(Class<?>... parms);
  public ConstructorBlockBuilder<BaseClassStructureBuilder> privateConstructor(Parameter... parms);

  public ConstructorBlockBuilder<BaseClassStructureBuilder> protectedConstructor();
  public ConstructorBlockBuilder<BaseClassStructureBuilder> protectedConstructor(MetaClass... parms);
  public ConstructorBlockBuilder<BaseClassStructureBuilder> protectedConstructor(Class<?>... parms);
  public ConstructorBlockBuilder<BaseClassStructureBuilder> protectedConstructor(Parameter... parms);

  public ConstructorBlockBuilder<BaseClassStructureBuilder> packageConstructor();
  public ConstructorBlockBuilder<BaseClassStructureBuilder> packageConstructor(MetaClass... parms);
  public ConstructorBlockBuilder<BaseClassStructureBuilder> packageConstructor(Class<?>... parms);
  public ConstructorBlockBuilder<BaseClassStructureBuilder> packageConstructor(Parameter... parms);

  // --- //
  public MethodBlockBuilder<BaseClassStructureBuilder> publicMethod(MetaClass returnType, String name);
  public MethodBlockBuilder<BaseClassStructureBuilder> publicMethod(Class<?> returnType, String name);
  public MethodBlockBuilder<BaseClassStructureBuilder> publicMethod(MetaClass returnType, String name, MetaClass... parms);
  public MethodBlockBuilder<BaseClassStructureBuilder> publicMethod(Class<?> returnType, String name, Class<?>... parms);
  public MethodBlockBuilder<BaseClassStructureBuilder> publicMethod(MetaClass returnType, String name, Parameter... parms);
  public MethodBlockBuilder<BaseClassStructureBuilder> publicMethod(Class<?> returnType, String name, Parameter... parms);
  
  public BlockBuilderImpl<BaseClassStructureBuilder> privateMethod(MetaClass returnType, String name);
  public BlockBuilderImpl<BaseClassStructureBuilder> privateMethod(Class<?> returnType, String name);
  public BlockBuilderImpl<BaseClassStructureBuilder> privateMethod(MetaClass returnType, String name, MetaClass... parms);
  public BlockBuilderImpl<BaseClassStructureBuilder> privateMethod(Class<?> returnType, String name, Class<?>... parms);
  public BlockBuilderImpl<BaseClassStructureBuilder> privateMethod(MetaClass returnType, String name, Parameter... parms);
  public BlockBuilderImpl<BaseClassStructureBuilder> privateMethod(Class<?> returnType, String name, Parameter... parms);
  
  public BlockBuilderImpl<BaseClassStructureBuilder> protectedMethod(MetaClass returnType, String name);
  public BlockBuilderImpl<BaseClassStructureBuilder> protectedMethod(Class<?> returnType, String name);
  public BlockBuilderImpl<BaseClassStructureBuilder> protectedMethod(MetaClass returnType, String name, MetaClass... parms);
  public BlockBuilderImpl<BaseClassStructureBuilder> protectedMethod(Class<?> returnType, String name, Class<?>... parms);
  public BlockBuilderImpl<BaseClassStructureBuilder> protectedMethod(MetaClass returnType, String name, Parameter... parms);
  public BlockBuilderImpl<BaseClassStructureBuilder> protectedMethod(Class<?> returnType, String name, Parameter... parms);
 
  public BlockBuilderImpl<BaseClassStructureBuilder> packageMethod(MetaClass returnType, String name);
  public BlockBuilderImpl<BaseClassStructureBuilder> packageMethod(Class<?> returnType, String name);
  public BlockBuilderImpl<BaseClassStructureBuilder> packageMethod(MetaClass returnType, String name, MetaClass... parms);
  public BlockBuilderImpl<BaseClassStructureBuilder> packageMethod(Class<?> returnType, String name, Class<?>... parms);
  public BlockBuilderImpl<BaseClassStructureBuilder> packageMethod(MetaClass returnType, String name, Parameter... parms);
  public BlockBuilderImpl<BaseClassStructureBuilder> packageMethod(Class<?> returnType, String name, Parameter... parms);

  // -- //
  public FieldBuildInitializer<BaseClassStructureBuilder> publicField(String name, MetaClass type);
  public FieldBuildInitializer<BaseClassStructureBuilder> publicField(String name, Class<?> type);

  public FieldBuildInitializer<BaseClassStructureBuilder> privateField(String name, MetaClass type);
  public FieldBuildInitializer<BaseClassStructureBuilder> privateField(String name, Class<?> type);

  public FieldBuildInitializer<BaseClassStructureBuilder> protectedField(String name, MetaClass type);
  public FieldBuildInitializer<BaseClassStructureBuilder> protectedField(String name, Class<?> type);

  public FieldBuildInitializer<BaseClassStructureBuilder> packageField(String name, MetaClass type);
  public FieldBuildInitializer<BaseClassStructureBuilder> packageField(String name, Class<?> type);
}