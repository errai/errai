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
public class WriteMapping implements MemberMapping {
  private MetaClass toMap;
  private String key;
  private MetaClass type;

  private MetaClassMember writingMember;

  public WriteMapping(Class<?> toMap, String key, Class<?> type, String getterMethod) {
    this(MetaClassFactory.get(toMap), key, MetaClassFactory.get(type), getterMethod);
  }

  public WriteMapping(MetaClass toMap, String key, MetaClass type, String getterMethod) {
    this.toMap = toMap;
    this.key = key;
    this.type = type;

    writingMember = toMap.getMethod(getterMethod, type);

    if (writingMember == null) {
      throw new RuntimeException("no such setter method: " + toMap.getFullyQualifiedName() + "." + getterMethod);
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
    return writingMember;
  }

  @Override
  public MetaClassMember getReadingMember() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean canRead() {
    return false;
  }

  @Override
  public boolean canWrite() {
    return true;
  }
}
