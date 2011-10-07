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
import org.jboss.errai.codegen.framework.VariableReference;
import org.jboss.errai.codegen.framework.builder.impl.Scope;
import org.jboss.errai.codegen.framework.exception.UndefinedFieldException;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaField;
import org.jboss.errai.codegen.framework.meta.impl.build.BuildMetaField;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class LoadField extends AbstractCallElement {
  private String fieldName;

  public LoadField(String fieldName) {
    this.fieldName = fieldName;
  }

  @Override
  public void handleCall(final CallWriter writer, final Context context, Statement statement) {
    final MetaField field;
    if (fieldName.equals("this")) {
      // TODO this is a workaround to access the enclosing instance of a type
      field = new BuildMetaField(null, null, Scope.Private, statement.getType(), "this");
    } 
    else {
      field = statement.getType().getField(fieldName);
    }

    if (field == null) {
      throw new UndefinedFieldException(fieldName, statement.getType());
    }

    final String currCallString = writer.getCallString();
    writer.reset();

    statement = new VariableReference() {

      @Override
      public String getName() {
        return field.getName();
      }

      @Override
      public Statement getValue() {
        return null;
      }

      @Override
      public String generate(Context context) {
        return currCallString + "." + getName();
      }

      @Override
      public MetaClass getType() {
        return field.getType();
      }
    };

    nextOrReturn(writer, context, statement);
  }

  @Override
  public String toString() {
    return "[[LoadField<" + fieldName + ">]" + next + "]";
  }
}
