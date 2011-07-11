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

import org.jboss.errai.ioc.rebind.ioc.codegen.AbstractStatement;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.DefParameters;
import org.jboss.errai.ioc.rebind.ioc.codegen.Parameter;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.BlockBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.BuildCallback;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.Finishable;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.UndefinedMethodException;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class AnonymousClassStructureBuilderImpl implements Finishable<ObjectBuilder> {
  private MetaClass toExtend;
  private BuildCallback<ObjectBuilder> callback;

  private List<DeferredGenerateCallback> callables = new ArrayList<DeferredGenerateCallback>();

  AnonymousClassStructureBuilderImpl(MetaClass clazz, BuildCallback<ObjectBuilder> builderBuildCallback) {
    this.toExtend = clazz;

    this.callback = builderBuildCallback;
  }

  private BlockBuilder<AnonymousClassStructureBuilderImpl> publicOverridesMethod(final MetaMethod method, final DefParameters parameters) {
    return new BlockBuilderImpl<AnonymousClassStructureBuilderImpl>(new BuildCallback<AnonymousClassStructureBuilderImpl>() {
      @Override
      public AnonymousClassStructureBuilderImpl callback(final Statement statement) {

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

        return AnonymousClassStructureBuilderImpl.this;
      }
    });
  }

  public BlockBuilder<AnonymousClassStructureBuilderImpl> publicOverridesMethod(String name, Parameter... args) {
    List<MetaClass> types = new ArrayList<MetaClass>();
    for (Parameter arg : args) {
      types.add(arg.getType());
    }
    MetaMethod method = toExtend.getBestMatchingMethod(name, types.toArray(new MetaClass[args.length]));
    if (method == null)
      throw new UndefinedMethodException("Method not found:" + name);
    
    return publicOverridesMethod(method, DefParameters.fromParameters(args));
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
      if (callables == null) 
        return null;

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