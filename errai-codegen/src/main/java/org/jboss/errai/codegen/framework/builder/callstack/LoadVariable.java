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

    VariableReference ref = (classMember) ? context.getClassMember(variableName) : context.getVariable(variableName);

    Statement[] indexes = new Statement[this.indexes.length];
    for (int i = 0; i < indexes.length; i++) {
      indexes[i] = GenUtil.generate(context, this.indexes[i]);
      indexes[i] = GenUtil.convert(context, indexes[i], MetaClassFactory.get(Integer.class));
    }
    ref.setIndexes(indexes);

    nextOrReturn(writer, context, ref);
  }

  @Override
  public String toString() {
    return "LoadVariable<" + variableName + ">]";
  }
}
