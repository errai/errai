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

package org.jboss.errai.codegen.framework.meta.impl.gwt;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.ext.typeinfo.TypeOracle;
import org.jboss.errai.codegen.framework.meta.MetaType;
import org.jboss.errai.codegen.framework.meta.impl.AbstractMetaParameterizedType;

import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JParameterizedType;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class GWTParameterizedType extends AbstractMetaParameterizedType {
  private JParameterizedType parameterizedType;
  private TypeOracle oracle;

  public GWTParameterizedType(TypeOracle oracle, JParameterizedType parameterizedType) {
    this.parameterizedType = parameterizedType;
    this.oracle = oracle;
  }

  @Override
  public MetaType[] getTypeParameters() {
    List<MetaType> types = new ArrayList<MetaType>();
    for (JClassType parm : parameterizedType.getTypeArgs()) {
      types.add(GWTClass.newInstance(oracle, parm));
    }
    return types.toArray(new MetaType[types.size()]);
  }

  @Override
  public MetaType getOwnerType() {
    return GWTClass.newInstance(oracle, parameterizedType.getEnclosingType());
  }

  @Override
  public MetaType getRawType() {
    return GWTClass.newInstance(oracle, parameterizedType.getRawType());
  }

  public String toString() {
    StringBuilder buf = new StringBuilder("<");
    JClassType[] parms = parameterizedType.getTypeArgs();
    for (int i = 0; i < parms.length; i++) {
      buf.append(GWTClass.newInstance(oracle, parms[i]).getFullyQualifiedName());
      if (i + 1 < parms.length) buf.append(',');
    }
    return buf.append('>').toString();
  }
}
