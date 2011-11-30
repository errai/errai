package org.jboss.errai.marshalling.rebind.api.model;

import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;

import java.util.ArrayList;
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
  }

  public MetaClass getMappingClass() {
    return toMap;
  }

  public void setConstructorMapping(ConstructorMapping mapping) {
    constructorMapping = mapping;
  }

  public void addMemberMapping(MemberMapping mapping) {
    memberMappings.add(mapping);
  }

  public ConstructorMapping getConstructorMapping() {
    return constructorMapping;
  }

  public List<MemberMapping> getMemberMappings() {
    return memberMappings;
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
