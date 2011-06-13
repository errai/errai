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

package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.AbstractStatement;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

public class Parameter extends AbstractStatement {
  private MetaClass type;
  private String name;

  public Parameter(MetaClass type, String name) {
    this.type = type;
    this.name = name;
  }

  public static Parameter of(MetaClass type, String name) {
    return new Parameter(type, name);
  }

  public String generate(Context context) {
    return type.getFullyQualifiedName() + " " + name;
  }

  public MetaClass getType() {
    return type;
  }

  public String getName() {
    return name;
  }
}
