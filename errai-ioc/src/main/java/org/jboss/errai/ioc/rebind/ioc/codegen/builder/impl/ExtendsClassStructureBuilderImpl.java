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
import java.util.concurrent.Callable;

import org.jboss.errai.ioc.rebind.ioc.codegen.*;
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
  private BuildCallback<ObjectBuilder> callback;

  private List<Callable<String>> callables = new ArrayList<Callable<String>>();

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
      public ExtendsClassStructureBuilderImpl callback(final Statement statement) {
        addCallable(new Callable<String>() {
          @Override
          public String call() throws Exception {
            StringBuilder buf = new StringBuilder();
            buf.append("public ").append(getClassReference(toExtend, classContext))
                    .append(parameters.generate(context)).append(" {\n");
            if (statement != null) {
              buf.append(statement.generate(context)).append("\n");
            }
            buf.append("}\n");
            return buf.toString();
          }
        });


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
      public ExtendsClassStructureBuilderImpl callback(final Statement statement) {

        addCallable(new Callable<String>() {
          @Override
          public String call() throws Exception {
            StringBuilder buf = new StringBuilder();
            buf.append("public ").append(getClassReference(method.getReturnType(), classContext))
                    .append(" ")
                    .append(method.getName())
                    .append(parameters.generate(context)).append(" {\n");
            if (statement != null) {
              buf.append(statement.generate(context)).append("\n");
            }
            buf.append("}\n");

            return buf.toString();
          }

        });


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
      return callback.callback(new AbstractStatement() {
        @Override
        public String generate(Context context) {
          return toJavaString();
        }
      });
    }

    return null;
  }

  private void addCallable(Callable<String> callable) {
    callables.add(callable);
  }

  @Override
  public String toJavaString() {
    try {
      if (callables == null) return null;

      StringBuilder buf = new StringBuilder();
      for (Callable<String> c : callables) {
        buf.append(c.call());
      }
      return buf.toString();
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
