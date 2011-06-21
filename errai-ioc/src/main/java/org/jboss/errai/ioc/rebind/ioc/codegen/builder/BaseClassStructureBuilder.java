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
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.BlockBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.FieldBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface BaseClassStructureBuilder extends Builder {
  public BlockBuilder<BaseClassStructureBuilder> publicConstructor(MetaClass... parms);

  public BlockBuilder<BaseClassStructureBuilder> publicConstructor(Class<?>... parms);

  public BlockBuilder<BaseClassStructureBuilder> publicConstructor(Parameter... parms);


  public BlockBuilder<BaseClassStructureBuilder> privateConstructor(MetaClass... parms);

  public BlockBuilder<BaseClassStructureBuilder> privateConstructor(Class<?>... parms);

  public BlockBuilder<BaseClassStructureBuilder> privateConstructor(Parameter... parms);


  public BlockBuilder<BaseClassStructureBuilder> protectedConstructor(MetaClass... parms);

  public BlockBuilder<BaseClassStructureBuilder> protectedConstructor(Class<?>... parms);

  public BlockBuilder<BaseClassStructureBuilder> protectedConstructor(Parameter... parms);


  public BlockBuilder<BaseClassStructureBuilder> packageConstructor(MetaClass... parms);

  public BlockBuilder<BaseClassStructureBuilder> packageConstructor(Class<?>... parms);

  public BlockBuilder<BaseClassStructureBuilder> packageConstructor(Parameter... parms);


  // --- //

  public BlockBuilder<BaseClassStructureBuilder> publicMethod(MetaClass returnType, String name, MetaClass... parms);

  public BlockBuilder<BaseClassStructureBuilder> publicMethod(Class<?> returnType, String name, Class<?>... parms);

  public BlockBuilder<BaseClassStructureBuilder> publicMethod(MetaClass returnType, String name, Parameter... parms);


  public BlockBuilder<BaseClassStructureBuilder> privateMethod(MetaClass returnType, String name, MetaClass... parms);

  public BlockBuilder<BaseClassStructureBuilder> privateMethod(Class<?> returnType, String name, Class<?>... parms);

  public BlockBuilder<BaseClassStructureBuilder> privateMethod(MetaClass returnType, String name, Parameter... parms);


  public BlockBuilder<BaseClassStructureBuilder> protectedMethod(MetaClass returnType, String name, MetaClass... parms);

  public BlockBuilder<BaseClassStructureBuilder> protectedMethod(Class<?> returnType, String name, Class<?>... parms);

  public BlockBuilder<BaseClassStructureBuilder>protectedMethod(MetaClass returnType, String name, Parameter... parms);


  public BlockBuilder<BaseClassStructureBuilder> packageMethod(MetaClass returnType, String name, MetaClass... parms);

  public BlockBuilder<BaseClassStructureBuilder> packageMethod(Class<?> returnType, String name, Class<?>... parms);

  public BlockBuilder<BaseClassStructureBuilder> packageMethod(MetaClass returnType, String name, Parameter... parms);


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
