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

import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.BlockBuilderImpl;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface ExtendsClassStructureBuilder extends BaseClassStructureBuilder {
  public BlockBuilderImpl<ExtendsClassStructureBuilder> publicOverridesConstructor(MetaClass... parms);

  public BlockBuilderImpl<ExtendsClassStructureBuilder> publicOverridesConstructor(Class<?>... parms);


  public BlockBuilderImpl<ExtendsClassStructureBuilder> privateOverridesConstructor(MetaClass... parms);

  public BlockBuilderImpl<ExtendsClassStructureBuilder> privateOverridesConstructor(Class<?>... parms);


  public BlockBuilderImpl<ExtendsClassStructureBuilder> protectedOverridesConstructor(MetaClass... parms);

  public BlockBuilderImpl<ExtendsClassStructureBuilder> protectedOverridesConstructor(Class<?>... parms);


  public BlockBuilderImpl<ExtendsClassStructureBuilder> packageOverridesConstructor(MetaClass... parms);

  public BlockBuilderImpl<ExtendsClassStructureBuilder> packageOverridesConstructor(Class<?>... parms);


  // -- //

  public BlockBuilderImpl<ExtendsClassStructureBuilder> publicOverridesMethod(MetaClass returnType, String name,
                                                                          MetaClass... parms);

  public BlockBuilderImpl<ExtendsClassStructureBuilder> publicOverridesMethod(Class<?> returnType, String name,
                                                                          Class<?>... parms);


  public BlockBuilderImpl<ExtendsClassStructureBuilder> privateOverridesMethod(MetaClass returnType, String name,
                                                                           MetaClass... parms);

  public BlockBuilderImpl<ExtendsClassStructureBuilder> privateOverridesMethod(Class<?> returnType,  String name,
                                                                           Class<?>... parms);


  public BlockBuilderImpl<ExtendsClassStructureBuilder> protectedOverridesMethod(MetaClass returnType, String name,
                                                                             MetaClass... parms);

  public BlockBuilderImpl<ExtendsClassStructureBuilder> protectedOverridesMethod(Class<?> returnType,  String name,
                                                                             Class<?>... parms);


  public BlockBuilderImpl<ExtendsClassStructureBuilder> packageOverridesMethod(MetaClass returnType, String name,
                                                                           MetaClass... parms);

  public BlockBuilderImpl<ExtendsClassStructureBuilder> packageOverridesMethod(Class<?> returnType,  String name,
                                                                           Class<?>... parms);

  // -- //


}
