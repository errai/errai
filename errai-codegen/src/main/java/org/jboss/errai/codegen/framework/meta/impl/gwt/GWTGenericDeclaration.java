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
import org.jboss.errai.codegen.framework.meta.MetaGenericDeclaration;
import org.jboss.errai.codegen.framework.meta.MetaTypeVariable;

import com.google.gwt.core.ext.typeinfo.JGenericType;
import com.google.gwt.core.ext.typeinfo.JTypeParameter;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class GWTGenericDeclaration implements MetaGenericDeclaration {
  private JGenericType genericType;
  private TypeOracle oracle;

  public GWTGenericDeclaration(TypeOracle oracle, JGenericType genericType) {
    this.oracle = oracle;
    this.genericType = genericType;
  }

  @Override
  public MetaTypeVariable[] getTypeParameters() {
    List<MetaTypeVariable> typeVariables = new ArrayList<MetaTypeVariable>();

    for (JTypeParameter typeParameter : genericType.getTypeParameters()) {
      typeVariables.add(new GWTTypeVariable(oracle, typeParameter));
    }

    return typeVariables.toArray(new MetaTypeVariable[typeVariables.size()]);
  }
}
