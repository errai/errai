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
public class AccessorMapping implements MemberMapping {
  private MetaClass toMap;
  private String key;
  private MetaClass type;

  private MetaClassMember bindingMember;
  private MetaClassMember readingMember;

  public AccessorMapping(Class<?> toMap, String key, Class<?> type, String setterMethod, String getterMethod) {
    this(MetaClassFactory.get(toMap), key, MetaClassFactory.get(type), setterMethod, getterMethod);
  }

  public AccessorMapping(MetaClass toMap, String key, MetaClass type, String setterMethod, String getterMethod) {
    this.toMap = toMap;
    this.key = key;
    this.type = type;

    bindingMember = toMap.getMethod(setterMethod, type);
    readingMember = toMap.getMethod(getterMethod, new MetaClass[0]);

    if (bindingMember == null) {
      throw new RuntimeException("no such setter method: " + toMap.getFullyQualifiedName() + "." + setterMethod);
    }

    if (readingMember == null) {
      throw new RuntimeException("no such getter method: " + toMap.getFullyQualifiedName() + "." + getterMethod);
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
  public MetaClassMember getBindingMember() {
    return bindingMember;
  }

  @Override
  public MetaClassMember getReadingMember() {
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
}
