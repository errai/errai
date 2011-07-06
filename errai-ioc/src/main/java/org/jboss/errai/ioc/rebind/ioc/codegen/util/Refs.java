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

package org.jboss.errai.ioc.rebind.ioc.codegen.util;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;
import org.jboss.errai.ioc.rebind.ioc.codegen.VariableReference;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.OutOfScopeException;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public abstract class Refs {
  public static VariableReference get(final String name) {
    return new VariableReference() {
      private MetaClass type;

      @Override
      public String getName() {
        return name;
      }

      @Override
      public Statement getValue() {
        return new Statement() {

          @Override
          public String generate(Context context) {
            VariableReference var = context.getVariable(name);

            if (var == null) {
              throw new OutOfScopeException("could not access variable: " + name);
            }

            type = var.getType();

            return name;
          }

          @Override
          public MetaClass getType() {
            return type;
          }
        };
      }

      @Override
      public String generate(Context context) {
        return getValue().generate(context);
      }

      @Override
      public MetaClass getType() {
        return type;
      }
    };

  }
}
