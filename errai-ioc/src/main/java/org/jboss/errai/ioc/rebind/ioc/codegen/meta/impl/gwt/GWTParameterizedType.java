/*
 * Copyright 2011 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.gwt;

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaParameterizedType;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaType;

import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JParameterizedType;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class GWTParameterizedType implements MetaParameterizedType {
  private JParameterizedType parameterizedType;

  public GWTParameterizedType(JParameterizedType parameterizedType) {
    this.parameterizedType = parameterizedType;
  }

  @Override
  public MetaType[] getTypeParameters() {
    List<MetaType> types = new ArrayList<MetaType>();
    for (JClassType parm : parameterizedType.getTypeArgs()) {
      types.add(MetaClassFactory.get(parm));
    }
    return types.toArray(new MetaType[types.size()]);
  }

  @Override
  public MetaType getOwnerType() {
    return MetaClassFactory.get(parameterizedType.getEnclosingType());
  }

  @Override
  public MetaType getRawType() {
    return MetaClassFactory.get(parameterizedType.getRawType());
  }
}
