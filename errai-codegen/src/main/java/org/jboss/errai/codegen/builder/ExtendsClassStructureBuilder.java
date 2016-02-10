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

package org.jboss.errai.codegen.builder;

import org.jboss.errai.codegen.meta.MetaClass;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@SuppressWarnings("rawtypes")
public interface ExtendsClassStructureBuilder extends ClassStructureBuilder, ClosedBlock {
  public BlockBuilder<ExtendsClassStructureBuilder> publicOverridesConstructor(MetaClass... parms);

  public BlockBuilder<ExtendsClassStructureBuilder> publicOverridesConstructor(Class<?>... parms);


  public BlockBuilder<ExtendsClassStructureBuilder> privateOverridesConstructor(MetaClass... parms);

  public BlockBuilder<ExtendsClassStructureBuilder> privateOverridesConstructor(Class<?>... parms);


  public BlockBuilder<ExtendsClassStructureBuilder> protectedOverridesConstructor(MetaClass... parms);

  public BlockBuilder<ExtendsClassStructureBuilder> protectedOverridesConstructor(Class<?>... parms);


  public BlockBuilder<ExtendsClassStructureBuilder> packageOverridesConstructor(MetaClass... parms);

  public BlockBuilder<ExtendsClassStructureBuilder> packageOverridesConstructor(Class<?>... parms);


  public BlockBuilder<ExtendsClassStructureBuilder> publicOverridesMethod(MetaClass returnType, String name,
                                                                          MetaClass... parms);

  public BlockBuilder<ExtendsClassStructureBuilder> publicOverridesMethod(Class<?> returnType, String name,
                                                                          Class<?>... parms);


  public BlockBuilder<ExtendsClassStructureBuilder> privateOverridesMethod(MetaClass returnType, String name,
                                                                           MetaClass... parms);

  public BlockBuilder<ExtendsClassStructureBuilder> privateOverridesMethod(Class<?> returnType, String name,
                                                                           Class<?>... parms);


  public BlockBuilder<ExtendsClassStructureBuilder> protectedOverridesMethod(MetaClass returnType, String name,
                                                                             MetaClass... parms);

  public BlockBuilder<ExtendsClassStructureBuilder> protectedOverridesMethod(Class<?> returnType, String name,
                                                                             Class<?>... parms);


  public BlockBuilder<ExtendsClassStructureBuilder> packageOverridesMethod(MetaClass returnType, String name,
                                                                           MetaClass... parms);

  public BlockBuilder<ExtendsClassStructureBuilder> packageOverridesMethod(Class<?> returnType, String name,
                                                                           Class<?>... parms);

}
