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
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.MethodBlockBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface ClassMethodBuilder<T extends ClassStructureBuilder<T>> extends ClassConstructorBuilder<T>,
                                                                                ClassFieldBuilder<T>, Builder {

  public MethodBlockBuilder<T> publicMethod(MetaClass returnType, String name);

  public MethodBlockBuilder<T> publicMethod(Class<?> returnType, String name);

  public MethodBlockBuilder<T> publicMethod(MetaClass returnType, String name, MetaClass... parms);

  public MethodBlockBuilder<T> publicMethod(Class<?> returnType, String name, Class<?>... parms);

  public MethodBlockBuilder<T> publicMethod(MetaClass returnType, String name, Parameter... parms);

  public MethodBlockBuilder<T> publicMethod(Class<?> returnType, String name, Parameter... parms);

  public MethodBlockBuilder<T> privateMethod(MetaClass returnType, String name);

  public MethodBlockBuilder<T> privateMethod(Class<?> returnType, String name);

  public MethodBlockBuilder<T> privateMethod(MetaClass returnType, String name, MetaClass... parms);

  public MethodBlockBuilder<T> privateMethod(Class<?> returnType, String name, Class<?>... parms);

  public MethodBlockBuilder<T> privateMethod(MetaClass returnType, String name, Parameter... parms);

  public MethodBlockBuilder<T> privateMethod(Class<?> returnType, String name, Parameter... parms);

  public MethodBlockBuilder<T> protectedMethod(MetaClass returnType, String name);

  public MethodBlockBuilder<T> protectedMethod(Class<?> returnType, String name);

  public MethodBlockBuilder<T> protectedMethod(MetaClass returnType, String name,
                                               MetaClass... parms);

  public MethodBlockBuilder<T> protectedMethod(Class<?> returnType, String name, Class<?>... parms);

  public MethodBlockBuilder<T> protectedMethod(MetaClass returnType, String name,
                                               Parameter... parms);

  public MethodBlockBuilder<T> protectedMethod(Class<?> returnType, String name, Parameter... parms);

  public MethodBlockBuilder<T> packageMethod(MetaClass returnType, String name);

  public MethodBlockBuilder<T> packageMethod(Class<?> returnType, String name);

  public MethodBlockBuilder<T> packageMethod(MetaClass returnType, String name, MetaClass... parms);

  public MethodBlockBuilder<T> packageMethod(Class<?> returnType, String name, Class<?>... parms);

  public MethodBlockBuilder<T> packageMethod(MetaClass returnType, String name, Parameter... parms);

  public MethodBlockBuilder<T> packageMethod(Class<?> returnType, String name, Parameter... parms);
}