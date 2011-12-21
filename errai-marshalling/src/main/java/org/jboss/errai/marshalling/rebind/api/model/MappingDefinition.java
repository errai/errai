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

package org.jboss.errai.marshalling.rebind.api.model;

import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.impl.java.JavaReflectionClass;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.rebind.api.model.impl.NoConstructMapping;
import org.jboss.errai.marshalling.rebind.api.model.impl.SimpleConstructorMapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Mike Brock
 */
public class MappingDefinition {
  private MetaClass toMap;
  private boolean marshal = true;
  private boolean demarshal = true;

  private boolean cachedMarshaller;
  
  private Class<? extends Marshaller> clientMarshallerClass;
  private Class<? extends Marshaller> serverMarshallerClass;
  
  private Marshaller<Object> marshallerInstance;

  private InstantiationMapping instantiationMapping;

  private List<MemberMapping> memberMappings = new ArrayList<MemberMapping>();

  public MappingDefinition(Marshaller<Object> marshaller) {
    this(marshaller, marshaller.getTypeHandled());
  }

  public MappingDefinition(Marshaller<Object> marshaller, Class<?> toMap) {
    this.toMap = JavaReflectionClass.newUncachedInstance(toMap);
    setMarshallerInstance(marshaller);
    instantiationMapping = new NoConstructMapping();
  }
  
  public MappingDefinition(Class<?> toMap) {
    this(JavaReflectionClass.newUncachedInstance(toMap));
  }

  public MappingDefinition(MetaClass toMap) {
    this.toMap = toMap;
    setInstantiationMapping(new SimpleConstructorMapping());
  }

  public MetaClass getMappingClass() {
    return toMap;
  }

  public Class<? extends Marshaller> getClientMarshallerClass() {
    return clientMarshallerClass;
  }

  public void setClientMarshallerClass(Class<? extends Marshaller> clientMarshallerClass) {
    this.clientMarshallerClass = clientMarshallerClass;
  }

  public Class<? extends Marshaller> getServerMarshallerClass() {
    return serverMarshallerClass;
  }

  public void setServerMarshallerClass(Class<? extends Marshaller> serverMarshallerClass) {
    this.serverMarshallerClass = serverMarshallerClass;
  }

  public void setInstantiationMapping(InstantiationMapping mapping) {
    mapping.setMappingClass(toMap);
    instantiationMapping = mapping;
  }

  public void setInheritedInstantiationMapping(InstantiationMapping mapping) {
    instantiationMapping = mapping;
  }

  public void addMemberMapping(MemberMapping mapping) {
    mapping.setMappingClass(toMap);
    memberMappings.add(mapping);
  }

  public void addInheritedMapping(MemberMapping mapping) {
    memberMappings.add(mapping);
  }

  public InstantiationMapping getInstantiationMapping() {
    return instantiationMapping;
  }

  public List<MemberMapping> getMemberMappings() {
    return memberMappings;
  }

  private volatile List<MemberMapping> _readableMemberMappingsCache;

  public List<MemberMapping> getReadableMemberMappings() {
    if (_readableMemberMappingsCache != null) {
      return _readableMemberMappingsCache;
    }

    List<MemberMapping> readableMemberMappings = new ArrayList<MemberMapping>();
    for (MemberMapping memberMapping : memberMappings) {
      if (memberMapping.canRead()) {
        readableMemberMappings.add(memberMapping);
      }
    }
    return _readableMemberMappingsCache = Collections.unmodifiableList(readableMemberMappings);
  }

  private volatile List<MemberMapping> _writableMemberMappingsCache;

  public List<MemberMapping> getWritableMemberMappings() {
    if (_writableMemberMappingsCache != null) {
      return _writableMemberMappingsCache;
    }

    List<MemberMapping> writableMemberMappings = new ArrayList<MemberMapping>();
    for (MemberMapping memberMapping : memberMappings) {
      if (memberMapping.canWrite()) {
        writableMemberMappings.add(memberMapping);
      }
    }
    return _writableMemberMappingsCache = Collections.unmodifiableList(writableMemberMappings);
  }

  public boolean canDemarshal() {
    return demarshal;
  }

  public boolean canMarshal() {
    return marshal;
  }

  public void setMarshal(boolean marshal) {
    this.marshal = marshal;
  }

  public void setDemarshal(boolean demarshal) {
    this.demarshal = demarshal;
  }

  public boolean isCachedMarshaller() {
    return cachedMarshaller;
  }

  public void setCachedMarshaller(boolean cachedMarshaller) {
    this.cachedMarshaller = cachedMarshaller;
  }

  public Marshaller<Object> getMarshallerInstance() {
    return marshallerInstance;
  }

  public void setMarshallerInstance(Marshaller<Object> marshallerInstance) {
    this.marshallerInstance = marshallerInstance;
    this.cachedMarshaller = marshallerInstance != null;
  }
}
