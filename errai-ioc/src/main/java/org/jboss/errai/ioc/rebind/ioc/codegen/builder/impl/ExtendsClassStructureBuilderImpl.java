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
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.BlockBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.BuildCallback;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.Finishable;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.LoadClassReference.getClassReference;

public class ExtendsClassStructureBuilderImpl implements Finishable<ObjectBuilder> {
  private MetaClass toExtend;
  private BuildCallback<ObjectBuilder> callback;

  private List<DeferredGenerateCallback> callables = new ArrayList<DeferredGenerateCallback>();

  ExtendsClassStructureBuilderImpl(MetaClass clazz, BuildCallback<ObjectBuilder> builderBuildCallback) {
    this.toExtend = clazz;

    this.callback = builderBuildCallback;
  }

  public BlockBuilder<ExtendsClassStructureBuilderImpl> publicConstructor(final DefParameters parameters) {
    return new BlockBuilderImpl<ExtendsClassStructureBuilderImpl>(new BuildCallback<ExtendsClassStructureBuilderImpl>() {
      @Override
      public ExtendsClassStructureBuilderImpl callback(final Statement statement) {
        addCallable(new DeferredGenerateCallback() {
          @Override
          public String doGenerate(Context context) {
            Context subContext = Context.create(context);

            for (Parameter parm : parameters.getParameters()) {
              subContext.addVariable(Variable.create(parm.getName(), parm.getType()));
            }

            StringBuilder buf = new StringBuilder();
            buf.append("public ").append(getClassReference(toExtend, context))
                    .append(parameters.generate(context)).append(" {\n");
            if (statement != null) {
              buf.append(statement.generate(subContext)).append("\n");
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
    return new BlockBuilderImpl<ExtendsClassStructureBuilderImpl>(new BuildCallback<ExtendsClassStructureBuilderImpl>() {
      @Override
      public ExtendsClassStructureBuilderImpl callback(final Statement statement) {

        addCallable(new DeferredGenerateCallback() {
          @Override
          public String doGenerate(Context context) {
            Context subContext = Context.create(context);
            for (Parameter parm : parameters.getParameters()) {
              subContext.addVariable(Variable.create(parm.getName(), parm.getType()));
            }

            StringBuilder buf = new StringBuilder();
            buf.append("public ").append(getClassReference(method.getReturnType(), context))
                    .append(" ")
                    .append(method.getName())
                    .append(parameters.generate(context)).append(" {\n");
            if (statement != null) {
              buf.append(statement.generate(subContext)).append("\n");
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
          return doGenerate(context);
        }
      });
    }

    return null;
  }

  private void addCallable(DeferredGenerateCallback callable) {
    callables.add(callable);
  }

  private String doGenerate(Context context) {
    try {
      if (callables == null) return null;

      StringBuilder buf = new StringBuilder();
      for (DeferredGenerateCallback c : callables) {
        buf.append(c.doGenerate(context));
      }
      return buf.toString();
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static interface DeferredGenerateCallback {
    public String doGenerate(Context context);
  }
}
