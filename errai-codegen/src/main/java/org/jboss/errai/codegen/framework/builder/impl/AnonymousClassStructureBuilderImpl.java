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

package org.jboss.errai.codegen.framework.builder.impl;

import static org.jboss.errai.codegen.framework.builder.callstack.LoadClassReference.getClassReference;

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.codegen.framework.AbstractStatement;
import org.jboss.errai.codegen.framework.Context;
import org.jboss.errai.codegen.framework.DefParameters;
import org.jboss.errai.codegen.framework.Parameter;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.Variable;
import org.jboss.errai.codegen.framework.builder.*;
import org.jboss.errai.codegen.framework.exception.UndefinedMethodException;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaMethod;
import org.jboss.errai.codegen.framework.meta.impl.build.BuildMetaClass;
import org.jboss.errai.codegen.framework.meta.impl.build.BuildMetaMethod;
import org.jboss.errai.codegen.framework.util.GenUtil;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class AnonymousClassStructureBuilderImpl
        extends ClassBuilder<AnonymousClassStructureBuilder>
        implements AnonymousClassStructureBuilder {
  private BuildCallback<ObjectBuilder> callback;
  private List<DeferredGenerateCallback> callables = new ArrayList<DeferredGenerateCallback>();
  private Context context;
  
  AnonymousClassStructureBuilderImpl(MetaClass clazz, BuildCallback<ObjectBuilder> builderBuildCallback) {
    super(clazz.getFullyQualifiedName(), clazz, builderBuildCallback.getParentContext());
    this.callback = builderBuildCallback;
    this.context = builderBuildCallback.getParentContext();
  }

  public BlockBuilder<AnonymousClassStructureBuilder> initialize() {
    return new BlockBuilderImpl<AnonymousClassStructureBuilder>(
            new BuildCallback<AnonymousClassStructureBuilder>() {
              @Override
              public AnonymousClassStructureBuilderImpl callback(final Statement statement) {

                addCallable(new DeferredGenerateCallback() {
                  @Override
                  public String doGenerate(Context context) {
                    StringBuilder buf = new StringBuilder();
                    buf.append("{\n");
                    if (statement != null) {
                      buf.append(statement.generate(Context.create(context))).append("\n");
                    }
                    buf.append("}\n");

                    return buf.toString();
                  }
                });

                return AnonymousClassStructureBuilderImpl.this;
              }

              @Override
              public Context getParentContext() {
                return  context;
              }
            });
  }

  private BlockBuilder<AnonymousClassStructureBuilder> publicOverridesMethod(final MetaMethod method,
                                                                             final DefParameters parameters) {
    return new BlockBuilderImpl<AnonymousClassStructureBuilder>(
            new BuildCallback<AnonymousClassStructureBuilder>() {
              @Override
              public AnonymousClassStructureBuilder callback(final Statement statement) {

                addCallable(new DeferredGenerateCallback() {
                  @Override
                  public String doGenerate(Context context) {
                    Context subContext = Context.create(context);
                    for (Parameter parm : parameters.getParameters()) {
                      subContext.addVariable(Variable.create(parm.getName(), parm.getType()));
                    }

                    StringBuilder buf = new StringBuilder();
                    String returnType = getClassReference(method.getReturnType(), context);

                    buf.append("public ").append(returnType)
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

              @Override
              public Context getParentContext() {
                return context;
              }
            });
  }

  public BlockBuilder<AnonymousClassStructureBuilder> publicOverridesMethod(String name, Parameter... args) {
    List<MetaClass> types = new ArrayList<MetaClass>();
    for (Parameter arg : args) {
      types.add(arg.getType());
    }
    MetaMethod method = classDefinition.getSuperClass()
            .getBestMatchingMethod(name, types.toArray(new MetaClass[args.length]));
    if (method == null)
      throw new UndefinedMethodException("Method not found:" + name);

    return publicOverridesMethod(method, DefParameters.from(method, args));
  }


  @Override
  public ObjectBuilder finish() {
    if (callback != null) {
      return callback.callback(new AbstractStatement() {
        @Override
        public String generate(Context context) {
          context.attachClass(getClassDefinition());
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

      Context subContext = Context.create(context);
      subContext.addVariable(Variable.create("this", getClassDefinition()));

      StringBuilder buf = new StringBuilder();
      for (DeferredGenerateCallback c : callables) {
        buf.append(c.doGenerate(subContext));
        buf.append('\n');
      }

      buf.append(classDefinition.membersToString());

      return buf.toString();
    }
    catch (Exception e) {
      GenUtil.throwIfUnhandled("while generating: " + classDefinition.getFullyQualifiedName(), e);
      return null;
    }
  }

  public static interface DeferredGenerateCallback {
    public String doGenerate(Context context);
  }


  public MetaClass getClassDefinition() {
    return classDefinition;
  }
}