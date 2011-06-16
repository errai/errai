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

import org.jboss.errai.ioc.rebind.ioc.codegen.Builder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.BlockBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface BaseClassStructureBuilder<T extends BaseClassStructureBuilder> extends Builder {
  public BlockBuilder<T> publicConstructor(MetaClass... parms);

  public BlockBuilder<T> publicConstructor(Class<?>... parms);

  public BlockBuilder<T> publicConstructor(Parameter... parms);


  public BlockBuilder<T> privateConstructor(MetaClass... parms);

  public BlockBuilder<T> privateConstructor(Class<?>... parms);

  public BlockBuilder<T> privateConstructor(Parameter... parms);


  public BlockBuilder<T> protectedConstructor(MetaClass... parms);

  public BlockBuilder<T> protectedConstructor(Class<?>... parms);

  public BlockBuilder<T> protectedConstructor(Parameter... parms);


  public BlockBuilder<T> packageConstructor(MetaClass... parms);

  public BlockBuilder<T> packageConstructor(Class<?>... parms);

  public BlockBuilder<T> packageConstructor(Parameter... parms);



  // --- //

  public BlockBuilder<T> publicMethod(MetaClass returnType, String name, MetaClass... parms);

  public BlockBuilder<T> publicMethod(Class<?> returnType, String name, Class<?>... parms);


  public BlockBuilder<T> privateMethod(MetaClass returnType, String name, MetaClass... parms);

  public BlockBuilder<T> privateMethod(Class<?> returnType, String name, Class<?>... parms);


  public BlockBuilder<T> protectedMethod(MetaClass returnType, String name, MetaClass... parms);

  public BlockBuilder<T> protectedMethod(Class<?> returnType, String name, Class<?>... parms);


  public BlockBuilder<T> packageMethod(MetaClass returnType, String name, MetaClass... parms);

  public BlockBuilder<T> packageMethod(Class<?> returnType, String name, Class<?>... parms);


  // -- //

  public FieldBuilder<T> publicField(String name, MetaClass type);

  public FieldBuilder<T> publicField(String name, Class<?> type);


  public FieldBuilder<T> privateField(String name, MetaClass type);

  public FieldBuilder<T> privateField(String name, Class<? > type);


  public FieldBuilder<T> protectedField(String name, MetaClass type);

  public FieldBuilder<T> protectedField(String name, Class<?> type);


  public FieldBuilder<T> packageField(String name, MetaClass type);

  public FieldBuilder<T> packageField(String name, Class<?> type);


}
