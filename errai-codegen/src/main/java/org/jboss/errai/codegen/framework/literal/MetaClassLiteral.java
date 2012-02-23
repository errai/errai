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

package org.jboss.errai.codegen.framework.literal;

import org.jboss.errai.codegen.framework.Context;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;

import static org.jboss.errai.codegen.framework.builder.callstack.LoadClassReference.getClassReference;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class MetaClassLiteral extends LiteralValue<MetaClass> {

  public MetaClassLiteral(MetaClass value) {
    super(value);
  }

  @Override
  public String getCanonicalString(Context context) {
    if (context == null) {
      return getValue().getName() + ".class";
    } else {
      return getClassReference(getValue(), context, false) + ".class";
    }
  }
  
  @Override
  public MetaClass getType() {
    return MetaClassFactory.get(Class.class);
  }
  
  public MetaClass getActualType() {
    return super.getType();
  }
}
