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

import org.jboss.errai.ioc.rebind.ioc.codegen.AbstractStatement;
import org.jboss.errai.ioc.rebind.ioc.codegen.BlockStatement;
import org.jboss.errai.ioc.rebind.ioc.codegen.CallParameters;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.StringStatement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.BlockBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.BuildCallback;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.ConstructorBlockBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.util.GenUtil;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class ConstructorBlockBuilderImpl<T extends ClassStructureBuilder<T>> extends BlockBuilderImpl<T> implements ConstructorBlockBuilder<T> {
  public ConstructorBlockBuilderImpl(BlockStatement blockStatement, BuildCallback<T> tBuildCallback) {
    super(blockStatement, tBuildCallback);
  }

  public ConstructorBlockBuilderImpl(BuildCallback<T> tBuildCallback) {
    super(tBuildCallback);
  }

  @Override
  public BlockBuilder<T> callSuper() {
    append(new StringStatement("super()"));
    return this;
  }

  @Override
  public BlockBuilder<T> callSuper(final Object... parameters) {
    append(new AbstractStatement() {
      @Override
      public String generate(Context context) {
        return "super" + 
          CallParameters.fromStatements(GenUtil.generateCallParameters(context, parameters)).generate(context);
      }
    });

    return this;
  }

  @Override
  public BlockBuilder<T> callThis(final Object... parameters) {
    append(new AbstractStatement() {
      @Override
      public String generate(Context context) {
        return "this" + 
          CallParameters.fromStatements(GenUtil.generateCallParameters(context, parameters)).generate(context);
      }
    });

    return this;
  }
}
