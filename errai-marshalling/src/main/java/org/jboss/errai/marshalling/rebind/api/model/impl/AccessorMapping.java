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

package org.jboss.errai.marshalling.rebind.api.model.impl;

import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.meta.MetaClassMember;
import org.jboss.errai.codegen.framework.meta.MetaMethod;
import org.jboss.errai.marshalling.rebind.api.model.MemberMapping;

/**
 * @author Mike Brock
 */
public class AccessorMapping implements MemberMapping {
  private MetaClass toMap;
  private String key;
  private MetaClass type;

  private MetaClassMember bindingMember;
  private MetaClassMember readingMember;
  
  private String setterMethod;
  private String getterMethod;

  public AccessorMapping(String key, Class<?> type, String setterMethod, String getterMethod) {
    this(key, MetaClassFactory.get(type), setterMethod, getterMethod);
  }

  public AccessorMapping(String key, MetaClass type, String setterMethod, String getterMethod) {
    this.key = key;
    this.type = type.asBoxed();

    this.setterMethod = setterMethod;
    this.getterMethod = getterMethod;
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
  public MetaClassMember getBindingMember() {
    if (bindingMember != null) {
      return bindingMember;
    }

    MetaMethod meth = toMap.getMethod(setterMethod, type);

    meth.asMethod().setAccessible(true);

    readingMember = meth;

    if (bindingMember == null) {
      throw new RuntimeException("no such setter method: " + toMap.getFullyQualifiedName() + "." + setterMethod);
    }

    return bindingMember;
  }

  @Override
  public MetaClassMember getReadingMember() {
    if (readingMember != null) {
      return readingMember;
    }

    MetaMethod meth = toMap.getMethod(getterMethod, new MetaClass[0]);

    meth.asMethod().setAccessible(true);
    
    readingMember = meth;
    
    if (readingMember == null) {
      throw new RuntimeException("no such getter method: " + toMap.getFullyQualifiedName() + "." + getterMethod);
    }

    
    
    return readingMember;
  }

  @Override
  public boolean canRead() {
    return true;
  }

  @Override
  public boolean canWrite() {
    return true;
  }

  @Override
  public void setMappingClass(MetaClass clazz) {
    this.toMap = clazz;
  }
}
