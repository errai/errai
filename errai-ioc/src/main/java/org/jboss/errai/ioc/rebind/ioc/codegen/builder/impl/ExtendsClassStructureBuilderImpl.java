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

import static org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.LoadClassReference.getClassReference;

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.DefParameters;
import org.jboss.errai.ioc.rebind.ioc.codegen.Parameter;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.StringStatement;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.BlockBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.BuildCallback;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.Builder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.Finishable;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaField;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;

public class ExtendsClassStructureBuilderImpl implements Builder, Finishable<ObjectBuilder> {
  private MetaClass toExtend;
  private Context classContext;
  private StringBuilder buf = new StringBuilder();
  private BuildCallback<ObjectBuilder> callback;

  ExtendsClassStructureBuilderImpl(MetaClass clazz, BuildCallback<ObjectBuilder> builderBuildCallback, Context parent) {
    this.toExtend = clazz;
    this.classContext = Context.create(parent);

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

    return new BlockBuilderImpl<ExtendsClassStructureBuilderImpl>(new BuildCallback<ExtendsClassStructureBuilderImpl>() {
      @Override
      public ExtendsClassStructureBuilderImpl callback(Statement statement) {
        buf.append("public ").append(getClassReference(toExtend, classContext))
            .append(parameters.generate(context)).append(" {\n");
        if (statement != null) {
          buf.append(statement.generate(context)).append("\n");
        }
        buf.append("}\n");

        return ExtendsClassStructureBuilderImpl.this;
      }
    });
  }

  private BlockBuilder<ExtendsClassStructureBuilderImpl> publicOverridesMethod(final MetaMethod method, final DefParameters parameters) {

    final Context context = Context.create(classContext);
    for (Parameter parm : parameters.getParameters()) {
      context.addVariable(Variable.create(parm.getName(), parm.getType()));
    }

    return new BlockBuilderImpl<ExtendsClassStructureBuilderImpl>(new BuildCallback<ExtendsClassStructureBuilderImpl>() {
      @Override
      public ExtendsClassStructureBuilderImpl callback(Statement statement) {

        buf.append("public ").append(getClassReference(method.getReturnType(), classContext))
            .append(" ")
            .append(method.getName())
            .append(parameters.generate(context)).append(" {\n");
        if (statement != null) {
          buf.append(statement.generate(context)).append("\n");
        }
        buf.append("}\n");

        return ExtendsClassStructureBuilderImpl.this;
      }
    });
  }

  public BlockBuilder<ExtendsClassStructureBuilderImpl> publicOverridesMethod(String name, Parameter... args) {
    List<MetaClass> types = new ArrayList<MetaClass>();
    for (Parameter arg : args) {
      types.add(arg.getType());
    }
    
    return publicOverridesMethod(toExtend.getBestMatchingMethod(name,
        types.toArray(new MetaClass[args.length])), DefParameters.fromParameters(args));
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
