package org.jboss.errai.marshalling.rebind.api.model.impl;

import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.meta.MetaClassMember;
import org.jboss.errai.marshalling.rebind.api.model.MemberMapping;

/**
 * @author Mike Brock
 */
public class SetterMapping implements MemberMapping {
  private MetaClass toMap;
  private String key;
  private MetaClass type;
  private MetaClassMember member;

  public SetterMapping(MetaClass toMap, String key, Class<?> type, String setterMethod) {
    this.toMap = toMap;
    this.key = key;
    this.type = MetaClassFactory.get(type);
    
    member = toMap.getMethod(setterMethod, type);

    if (member == null) {
      throw new RuntimeException("no such setter method: " + toMap.getFullyQualifiedName() + "." + setterMethod);
    }
  }

  @Override
  public String getKey() {
    return key;
  }

  @Override
  public MetaClass getType() {
    return type;
  }

  @Override
  public MetaClassMember getMember() {
    return member;
  }
}
