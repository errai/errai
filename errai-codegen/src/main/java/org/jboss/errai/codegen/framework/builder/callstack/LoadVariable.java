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
import org.jboss.errai.codegen.framework.exception.InvalidTypeException;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.util.GenUtil;

/**
 * {@link CallElement} to load {@link VariableReference}s. Indexes can be provided in case of an array.
 *
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class LoadVariable extends AbstractCallElement {
  private String variableName;
  private Object[] indexes;
  private boolean classMember;

  public LoadVariable(String variableName, Object... indexes) {
    this.variableName = variableName;
    this.indexes = indexes;
  }

  public LoadVariable(String variableName, boolean classMember, Object... indexes) {
    this(variableName, indexes);
    this.classMember = classMember;
  }

  @Override
  public void handleCall(CallWriter writer, Context context, Statement statement) {
    writer.reset();

    final Statement[] idx = new Statement[this.indexes.length];
    for (int i = 0; i < idx.length; i++) {
      idx[i] = GenUtil.generate(context, this.indexes[i]);
      idx[i] = GenUtil.convert(context, idx[i], MetaClassFactory.get(Integer.class));
    }

    final VariableReference ref = context.getVariable(variableName);

    if (idx.length > 0) {
      if (!ref.getType().isArray()) {
        throw new InvalidTypeException("attempt to use indexed accessor on non-array type: " + ref);
      }
//      else if (GenUtil.getArrayDimensions(ref.getType()) != idx.length) {
//        throw new InvalidTypeException("wrong number of dimension for array type " + ref);
//      }
    }

    final Statement stmt = new VariableReference() {
      @Override
      public String getName() {
        return ref.getName();
      }

      @Override
      public Statement getValue() {
        return ref.getValue();
      }

      @Override
      public String generate(Context context) {
        StringBuilder buf = new StringBuilder((classMember
                && !context.isNonAmbiguous(ref.getName()) ? "this." : "") + getName());
        
        for (Statement s : idx) {
          buf.append('[').append(s.generate(context)).append(']');
        }

        return buf.toString();
      }

      @Override
      public MetaClass getType() {
        MetaClass ret;
        
        int dims = GenUtil.getArrayDimensions(ref.getType());

        if (ref.getType().isArray() && idx.length > 0) {
          int newDims = dims - idx.length;
          if (newDims > 0) {
            ret = ref.getType().getOuterComponentType().asArrayOf(dims - idx.length);
          }
          else {
            ret = ref.getType().getOuterComponentType();
          }
        }
        else {
          ret = ref.getType();
        }
        
        return ret;
      }
    };

    ref.setIndexes(idx);

    nextOrReturn(writer, context, stmt);
  }

  @Override
  public String toString() {
    return "[[LoadVariable<" + variableName + ">]" + next + "]";
  }
}
