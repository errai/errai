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
import org.jboss.errai.marshalling.rebind.api.model.MemberMapping;

/**
 * @author Mike Brock
 */
public class ReadMapping implements MemberMapping {
  private MetaClass toMap;
  
  private String key;
  private MetaClass type;

  private MetaClassMember readingMember;

  private String getterMethod;
  
  public ReadMapping(String key, Class<?> type,  String getterMethod) {
    this(key, MetaClassFactory.get(type), getterMethod);
  }

  public ReadMapping( String key, MetaClass type, String getterMethod) {
    this.key = key;
    this.type = type.asBoxed();

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
    throw new UnsupportedOperationException();
  }

  @Override
  public MetaClassMember getReadingMember() {
    if (readingMember != null) {
    return readingMember;
    }
    
    readingMember = toMap.getMethod(getterMethod, new MetaClass[0]);

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
    return false;
  }

  @Override
  public void setMappingClass(MetaClass clazz) {
    this.toMap = clazz;
  }
}
