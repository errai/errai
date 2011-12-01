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
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
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

  private ConstructorMapping constructorMapping;
  private List<MemberMapping> memberMappings = new ArrayList<MemberMapping>();

  public MappingDefinition(Class<?> toMap) {
    this(MetaClassFactory.get(toMap));
  }

  public MappingDefinition(MetaClass toMap) {
    this.toMap = toMap;
    setConstructorMapping(new SimpleConstructorMapping());
  }

  public MappingDefinition(MetaClass toMap, ConstructorMapping cMapping) {
    this.toMap = toMap;
    this.constructorMapping = cMapping;
  }

  public MappingDefinition(Class<?> toMap, ConstructorMapping cMapping) {
    this(MetaClassFactory.get(toMap), cMapping);
  }


  public MetaClass getMappingClass() {
    return toMap;
  }

  public void setConstructorMapping(ConstructorMapping mapping) {
    mapping.setMappingClass(toMap);
    constructorMapping = mapping;
  }

  public void setInheritedConstructorMapping(ConstructorMapping mapping) {
    constructorMapping = mapping;
  }


  public void addMemberMapping(MemberMapping mapping) {
    mapping.setMappingClass(toMap);
    memberMappings.add(mapping);
  }
  
  public void addInheritedMapping(MemberMapping mapping) {
    memberMappings.add(mapping);
  }

  public ConstructorMapping getConstructorMapping() {
    return constructorMapping;
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
    return _readableMemberMappingsCache = Collections.unmodifiableList(readableMemberMappings) ;
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
    return _writableMemberMappingsCache = Collections.unmodifiableList(writableMemberMappings) ;
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
}
