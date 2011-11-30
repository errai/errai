/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

import org.jboss.errai.codegen.framework.AbstractStatement;
import org.jboss.errai.codegen.framework.BlockStatement;
import org.jboss.errai.codegen.framework.CallParameters;
import org.jboss.errai.codegen.framework.Context;
import org.jboss.errai.codegen.framework.StringStatement;
import org.jboss.errai.codegen.framework.builder.BlockBuilder;
import org.jboss.errai.codegen.framework.builder.BuildCallback;
import org.jboss.errai.codegen.framework.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.framework.builder.ConstructorBlockBuilder;
import org.jboss.errai.codegen.framework.util.GenUtil;

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

      String generatedCache;

      @Override
      public String generate(Context context) {
        if (generatedCache != null) return generatedCache;
        return generatedCache = "this" +
                CallParameters.fromStatements(GenUtil.generateCallParameters(context, parameters)).generate(context);
      }
    });

    return this;
  }
}
