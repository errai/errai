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

package org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl;

import java.util.Map;

import javax.enterprise.util.TypeLiteral;

import org.jboss.errai.ioc.rebind.ioc.codegen.Builder;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.VariableDeclaration;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.LoadClassReference;

/**
 * Builder for the {@link Context}.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ContextBuilder implements Builder {
  private Context context;

  protected ContextBuilder(Context context) {
    this.context = context;
  }

  public static ContextBuilder create() {
    return new ContextBuilder(Context.create());
  }

  public static ContextBuilder create(Context context) {
    return new ContextBuilder(context);
  }

  public ContextBuilder addVariable(Variable variable) {
    context.addVariable(variable);
    return this;
  }

  public ContextBuilder addVariable(String name, Class<?> type) {
    context.addVariable(Variable.create(name, type));
    return this;
  }

  public ContextBuilder addVariable(String name, TypeLiteral<?> type) {
    context.addVariable(Variable.create(name, type));
    return this;
  }

  public ContextBuilder addVariable(String name, Object initialization) {
    context.addVariable(Variable.create(name, initialization));
    return this;
  }

  public ContextBuilder addVariable(String name, Class<?> type, Object initialization) {
    context.addVariable(Variable.create(name, type, initialization));
    return this;
  }

  public ContextBuilder addVariable(String name, TypeLiteral<?> type, Object initialization) {
    context.addVariable(Variable.create(name, type, initialization));
    return this;
  }

  public VariableDeclaration declareVariable(final Variable var) {
    context.addVariable(var);
    return new VariableDeclaration() {
      public Statement initializeWith(Object initialization) {
        var.initialize(initialization);
        return var;
      }

      public Statement initializeWith(Statement initialization) {
        var.initialize(initialization);
        return var;
      }
    };
  }

  public VariableDeclaration declareVariable(String name) {
    return declareVariable(Variable.create(name, (Class<?>) null));
  }

  public VariableDeclaration declareVariable(String name, Class<?> type) {
    return declareVariable(Variable.create(name, type));
  }

  public VariableDeclaration declareVariable(String name, TypeLiteral<?> type) {
    return declareVariable(Variable.create(name, type));
  }


  public String toJavaString() {
    Map<String, Variable> vars = context.getVariables();
    StringBuilder buf = new StringBuilder();

    for (Map.Entry<String, Variable> entry : vars.entrySet()) {
      buf.append(LoadClassReference.getClassReference(entry.getValue().getType(), context))
      .append(" ")
      .append(entry.getKey());

      if (entry.getValue().getValue() != null) {
        buf.append(entry.getValue().generate(context));
      }
      buf.append(';');
      buf.append('\n');
    }

    return buf.toString();
  }

  public Context getContext() {
    return context;
  }
}