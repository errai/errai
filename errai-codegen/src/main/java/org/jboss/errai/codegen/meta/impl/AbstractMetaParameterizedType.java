/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.codegen.meta.impl;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaParameterizedType;
import org.jboss.errai.codegen.meta.MetaType;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public abstract class AbstractMetaParameterizedType implements MetaParameterizedType {
  @Override
  public boolean isAssignableFrom(final MetaParameterizedType type) {
    final MetaType[] compareFrom = getTypeParameters();
    final MetaType[] compareTo;

    if (type == null) {
      compareTo = new MetaType[compareFrom.length];
      for (int i = 0; i < compareFrom.length; i++) {
        compareTo[i] = new MetaType() {
          @Override
          public String toString() {
            return getName();
          }

          @Override
          public String getName() {
            return "?";
          }
        };
      }
    }
    else {
      compareTo = type.getTypeParameters();
    }

    if (compareTo.length != compareFrom.length) return false;

    for (int i = 0; i < compareTo.length; i++) {
      if (compareFrom[i].toString().equals("?")) {
        continue;
      }
      if (compareFrom[i] instanceof MetaClass && compareTo[i] instanceof MetaClass) {
        if (!((MetaClass) compareFrom[i]).isAssignableFrom((MetaClass) compareTo[i])) {
          return false;
        }
      }
    }

    return true;
  }

  @Override
  public final String toString() {
    final StringBuilder buf = new StringBuilder("<");
    final MetaType[] parms = getTypeParameters();
    for (int i = 0; i < parms.length; i++) {
      if (parms[i] instanceof MetaClass) {
        buf.append(((MetaClass) parms[i]).getFullyQualifiedName());
      }
      else {
        buf.append(parms[i].toString());
      }
      if (i + 1 < parms.length) buf.append(',');
    }
    return buf.append('>').toString();
  }

}
