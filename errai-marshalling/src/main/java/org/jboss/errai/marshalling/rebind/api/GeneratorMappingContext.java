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

import org.jboss.errai.codegen.framework.Context;
import org.jboss.errai.codegen.framework.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassMember;
import org.jboss.errai.codegen.framework.meta.MetaField;
import org.jboss.errai.codegen.framework.meta.MetaMethod;
import org.jboss.errai.codegen.framework.util.GenUtil;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.rebind.DefinitionsFactory;
import org.jboss.errai.marshalling.rebind.DefinitionsFactoryImpl;
import org.jboss.errai.marshalling.server.ServerMappingContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class GeneratorMappingContext implements ServerMappingContext {

  private final DefinitionsFactory definitionsFactory = new DefinitionsFactoryImpl();

  private Set<String> generatedMarshallers = new HashSet<String>();
  private List<String> renderedMarshallers = new ArrayList<String>();

  private Context codegenContext;

  private MetaClass generatedBootstrapClass;
  private ClassStructureBuilder<?> classStructureBuilder;
  private ArrayMarshallerCallback arrayMarshallerCallback;

  private Set<String> exposedMembers = new HashSet<String>();

  public GeneratorMappingContext(Context codegenContext, MetaClass generatedBootstrapClass,
                                 ClassStructureBuilder<?> classStructureBuilder,
                                 ArrayMarshallerCallback callback) {

    this.codegenContext = codegenContext;
    this.generatedBootstrapClass = generatedBootstrapClass;
    this.classStructureBuilder = classStructureBuilder;
    this.arrayMarshallerCallback = callback;
  }


  @Override
  public DefinitionsFactory getDefinitionsFactory() {
    return definitionsFactory;
  }

  public Class<? extends Marshaller> getMarshallerClass(String clazzName) {
    if (definitionsFactory.getMappingAliases().containsKey(clazzName)) {
      clazzName = definitionsFactory.getMappingAliases().get(clazzName);
    }

    return definitionsFactory.getDefinition(clazzName).getClientMarshallerClass();
  }

  public void registerGeneratedMarshaller(String clazzName) {
    generatedMarshallers.add(clazzName);
  }

  public boolean hasMarshaller(MetaClass clazz) {
    if (clazz.isArray()) {
      clazz = clazz.getOuterComponentType();
    }

    if (clazz.isPrimitive()) {
      clazz = clazz.asBoxed();
    }

    return hasMarshaller(clazz.getFullyQualifiedName());
  }

  public boolean hasMarshaller(String clazzName) {
    return definitionsFactory.hasDefinition(clazzName);
  }

  @Override
  public Marshaller<Object> getMarshaller(String clazz) {
    return null;
  }

  public boolean hasGeneratedMarshaller(MetaClass clazz) {
    if (clazz.isArray()) {
      clazz = clazz.getOuterComponentType();
    }
    if (clazz.isPrimitive()) {
      clazz = clazz.asBoxed();
    }

    return hasGeneratedMarshaller(clazz.getFullyQualifiedName());
  }

  private boolean hasGeneratedMarshaller(String clazzName) {
    return generatedMarshallers.contains(clazzName);
  }

  public boolean canMarshal(String clazz) {
    return hasMarshaller(clazz) || hasGeneratedMarshaller(clazz);
  }

  public Context getCodegenContext() {
    return codegenContext;
  }

  public void markRendered(Class<?> clazz) {
    markRendered(clazz.getName());
  }

  public void markRendered(String className) {
    renderedMarshallers.add(className);
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

  private static String getPrivateMemberName(MetaClassMember member) {
    if (member instanceof MetaField) {
      return GenUtil.getPrivateFieldInjectorName((MetaField) member);
    }
    else {
      return GenUtil.getPrivateMethodName((MetaMethod) member);
    }
  }

  public void markExposed(MetaClassMember member) {
    exposedMembers.add(getPrivateMemberName(member));
  }

  public boolean isExposed(MetaClassMember member) {
    return exposedMembers.contains(getPrivateMemberName(member));
  }
}
