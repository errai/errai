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

package org.jboss.errai.codegen.framework.meta.impl.java;

import java.lang.reflect.TypeVariable;

import org.jboss.errai.codegen.framework.meta.MetaGenericDeclaration;
import org.jboss.errai.codegen.framework.meta.MetaType;
import org.jboss.errai.codegen.framework.meta.MetaTypeVariable;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class JavaReflectionTypeVariable implements MetaTypeVariable {
  private TypeVariable variable;
  private MetaGenericDeclaration declaration;

  public JavaReflectionTypeVariable(TypeVariable variable) {
    this.variable = variable;
    this.declaration = new JavaReflectionGenericDeclaration(variable.getGenericDeclaration());
  }

  @Override
  public MetaType[] getBounds() {
    return JavaReflectionUtil.fromTypeArray(variable.getBounds());
  }

  @Override
  public MetaGenericDeclaration getGenericDeclaration() {
    return declaration;
  }

  @Override
  public String getName() {
    return variable.getName();
  }
}
