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

import org.jboss.errai.ioc.rebind.ioc.codegen.*;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.BuildCallback;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.Builder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.Finishable;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaField;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;

import static org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.LoadClassReference.getClassReference;

public class ExtendsClassStructureBuilderImpl implements Builder, Finishable<ObjectBuilder> {
  private MetaClass toExtend;
  private Context classContext;
  private StringBuilder buf = new StringBuilder();
  private BuildCallback<ObjectBuilder> callback;

  ExtendsClassStructureBuilderImpl(MetaClass clazz, BuildCallback<ObjectBuilder> builderBuildCallback) {
    this.toExtend = clazz;
    this.classContext = Context.create();

    for (MetaField field : clazz.getFields()) {
      this.classContext.addVariable(Variable.create(field.getName(), field.getType()));
    }

    this.callback = builderBuildCallback;
  }

  public BlockBuilder<ExtendsClassStructureBuilderImpl> publicConstructor(final DefParameters parameters) {
    final Context context = Context.create(classContext);
    for (Parameter parm : parameters.getParameters()) {
      context.addVariable(Variable.create(parm.getName(), parm.getType()));
    }

    return new BlockBuilder<ExtendsClassStructureBuilderImpl>(new BuildCallback<ExtendsClassStructureBuilderImpl>() {
      @Override
      public ExtendsClassStructureBuilderImpl callback(Statement statement) {
        buf.append("public ").append(getClassReference(toExtend, classContext))
            .append(parameters.generate(context)).append(" {\n");
        if (statement != null) {
          buf.append(statement.generate(Context.create(classContext))).append("\n");
        }
        buf.append("}\n");

        return ExtendsClassStructureBuilderImpl.this;
      }
    });
  }

  public BlockBuilder<ExtendsClassStructureBuilderImpl> publicOverridesMethod(final MetaMethod method) {
    final DefParameters parameters = DefParameters.from(method);

    final Context context = Context.create(classContext);
    for (Parameter parm : parameters.getParameters()) {
      context.addVariable(Variable.create(parm.getName(), parm.getType()));
    }

    return new BlockBuilder<ExtendsClassStructureBuilderImpl>(new BuildCallback<ExtendsClassStructureBuilderImpl>() {
      @Override
      public ExtendsClassStructureBuilderImpl callback(Statement statement) {

        Context ctx = Context.create(context);

        buf.append("public ").append(getClassReference(method.getReturnType(), classContext))
            .append(" ")
            .append(method.getName())
            .append(parameters.generate(ctx)).append(" {\n");
        if (statement != null) {
          buf.append(statement.generate(ctx)).append("\n");
        }
        buf.append("}\n");

        return ExtendsClassStructureBuilderImpl.this;
      }
    });
  }

  public BlockBuilder<ExtendsClassStructureBuilderImpl> publicOverridesMethod(String name, Class... args) {
    return publicOverridesMethod(toExtend.getBestMatchingMethod(name, args));
  }

  @Override
  public ObjectBuilder finish() {
    if (callback != null) {
      return callback.callback(new StringStatement(toJavaString()));
    }

    return null;
  }

  @Override
  public String toJavaString() {
    return buf.toString();
  }
}
