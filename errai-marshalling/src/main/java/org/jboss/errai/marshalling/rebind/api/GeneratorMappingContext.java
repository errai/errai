/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.jboss.errai.marshalling.rebind.api;

import org.jboss.errai.codegen.Context;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassMember;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.util.PrivateAccessUtil;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.rebind.DefinitionsFactory;
import org.jboss.errai.marshalling.rebind.DefinitionsFactorySingleton;
import org.jboss.errai.marshalling.server.ServerMappingContext;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class GeneratorMappingContext implements ServerMappingContext {

  private final DefinitionsFactory definitionsFactory = DefinitionsFactorySingleton.get();

  private final Set<String> generatedMarshallers = new HashSet<String>();

  private final Context codegenContext;

  private final MetaClass generatedBootstrapClass;
  private final ClassStructureBuilder<?> classStructureBuilder;
  private final ArrayMarshallerCallback arrayMarshallerCallback;

  private final Set<String> exposedMembers = new HashSet<String>();

  public GeneratorMappingContext(final Context codegenContext,
                                 final MetaClass generatedBootstrapClass,
                                 final ClassStructureBuilder<?> classStructureBuilder,
                                 final ArrayMarshallerCallback callback) {

    this.codegenContext = codegenContext;
    this.generatedBootstrapClass = generatedBootstrapClass;
    this.classStructureBuilder = classStructureBuilder;
    this.arrayMarshallerCallback = callback;
  }


  @Override
  public DefinitionsFactory getDefinitionsFactory() {
    return definitionsFactory;
  }

  public void registerGeneratedMarshaller(final String clazzName) {
    generatedMarshallers.add(clazzName);
  }


  public boolean hasMarshaller(final String clazzName) {
    return definitionsFactory.hasDefinition(clazzName);
  }

  @Override
  public Marshaller<Object> getMarshaller(final String clazz) {
    return null;
  }

  private boolean hasGeneratedMarshaller(final String clazzName) {
    return generatedMarshallers.contains(clazzName);
  }

  public boolean canMarshal(final String clazz) {
    return hasMarshaller(clazz) || hasGeneratedMarshaller(clazz);
  }

  public Context getCodegenContext() {
    return codegenContext;
  }

  public MetaClass getGeneratedBootstrapClass() {
    return generatedBootstrapClass;
  }

  public ClassStructureBuilder<?> getClassStructureBuilder() {
    return classStructureBuilder;
  }

  public ArrayMarshallerCallback getArrayMarshallerCallback() {
    return arrayMarshallerCallback;
  }

  private static String getPrivateMemberName(final MetaClassMember member) {
    if (member instanceof MetaField) {
      return PrivateAccessUtil.getPrivateFieldInjectorName((MetaField) member);
    }
    else {
      return PrivateAccessUtil.getPrivateMethodName((MetaMethod) member);
    }
  }

  public void markExposed(final MetaClassMember member) {
    exposedMembers.add(getPrivateMemberName(member));
  }

  public boolean isExposed(final MetaClassMember member) {
    return exposedMembers.contains(getPrivateMemberName(member));
  }
}
