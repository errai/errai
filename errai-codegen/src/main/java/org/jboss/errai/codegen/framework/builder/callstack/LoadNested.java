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

package org.jboss.errai.codegen.framework.builder.callstack;

import org.jboss.errai.codegen.framework.Context;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.meta.MetaClass;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class LoadNested extends AbstractCallElement {
  private Statement statement;

  public LoadNested(final Statement statement) {
    this.statement = new Statement() {
      MetaClass type;

      @Override
      public String generate(Context context) {
        String res = statement.generate(context).trim();

        type = statement.getType();

        if (getNext() == null || (getNext() instanceof ReturnValue)) {
          return res;
        }
        else if (isIdentifierOnly(res)) {
          return res;
        }
        else {
          return "(" + res + ")";
        }
      }

      @Override
      public MetaClass getType() {
        return type;
      }

      @Override
      public String toString() {
        return statement.toString();
      }
    };
  }

  private static boolean isIdentifierOnly(String s) {
    for (int i = 0; i < s.length(); i++) {
      if (!Character.isJavaIdentifierPart(s.charAt(0))) return false;
    }
    
    return true;
  }
  
  @Override
  public void handleCall(CallWriter writer, Context context, Statement statement) {
    nextOrReturn(writer, context, this.statement);
  }

  @Override
  public String toString() {
    return "[[Nest<" + statement + ">]" + next + "]";
  }
}
