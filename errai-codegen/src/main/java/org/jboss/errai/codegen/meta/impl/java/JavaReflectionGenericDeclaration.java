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

package org.jboss.errai.codegen.meta.impl.java;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.codegen.meta.MetaGenericDeclaration;
import org.jboss.errai.codegen.meta.MetaTypeVariable;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class JavaReflectionGenericDeclaration implements MetaGenericDeclaration {
  private final GenericDeclaration genericDeclaration;

  public JavaReflectionGenericDeclaration(GenericDeclaration genericDeclaration) {
    this.genericDeclaration = genericDeclaration;
  }

  @Override
  public MetaTypeVariable[] getTypeParameters() {
    List<MetaTypeVariable> metaTypeVariableList = new ArrayList<MetaTypeVariable>();

    for (TypeVariable<?> typeVariable : genericDeclaration.getTypeParameters()) {
      metaTypeVariableList.add(new JavaReflectionTypeVariable(typeVariable));
    }

    return metaTypeVariableList.toArray(new MetaTypeVariable[metaTypeVariableList.size()]);
  }

  @Override
  public String getName() {
    return genericDeclaration.toString();
  }
}
