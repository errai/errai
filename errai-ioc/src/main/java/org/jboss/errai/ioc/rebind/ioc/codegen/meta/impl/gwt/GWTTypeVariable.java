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

import com.google.gwt.core.ext.typeinfo.JTypeParameter;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaGenericDeclaration;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaType;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaTypeVariable;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class GWTTypeVariable implements MetaTypeVariable {
  private JTypeParameter typeParameter;

  public GWTTypeVariable(JTypeParameter typeParameter) {
    this.typeParameter = typeParameter;
  }

  public MetaType[] getBounds() {
    return GWTUtil.fromTypeArray(typeParameter.getBounds());
  }

  public MetaGenericDeclaration getGenericDeclaration() {
    if (typeParameter.isGenericType() != null) {
      return new GWTGenericDeclaration(typeParameter.isGenericType());
    }
    return null;
  }

  public String getName() {
    return typeParameter.getName();
  }
}
