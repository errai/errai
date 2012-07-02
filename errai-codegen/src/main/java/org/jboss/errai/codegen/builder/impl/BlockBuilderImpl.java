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

package org.jboss.errai.codegen.builder.impl;

import org.jboss.errai.codegen.BlockStatement;
import org.jboss.errai.codegen.Context;
import org.jboss.errai.codegen.InnerClass;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.BuildCallback;
import org.jboss.errai.codegen.meta.MetaClass;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class BlockBuilderImpl<T> implements BlockBuilder<T> {
  protected final BlockStatement blockStatement;
  protected final BuildCallback<T> callback;

  public BlockBuilderImpl(final BuildCallback<T> callback) {
    this(new BlockStatement(), callback);
  }

  public BlockBuilderImpl(final BlockStatement blockStatement, final BuildCallback<T> callback) {
    this.blockStatement = blockStatement;
    this.callback = callback;
  }

  @Override
  public void insertBefore(final Statement stmt) {
    this.blockStatement.insertBefore(stmt);
  }

  @Override
  public void insertBefore(final InnerClass innerClass) {
    insertBefore(new Statement() {

      @Override
      public String generate(final Context context) {
        return innerClass.generate(context);
      }

      @Override
      public MetaClass getType() {
        return innerClass.getType();
      }
    });
  }

  @Override
  public BlockBuilder<T> append(final Statement statement) {
    blockStatement.addStatement(statement);
    return this;
  }

  @Override
  public BlockBuilder<T> append(final InnerClass innerClass) {
    blockStatement.addStatement(new Statement() {

      @Override
      public String generate(final Context context) {
        return innerClass.generate(context);
      }

      @Override
      public MetaClass getType() {
        return innerClass.getType();
      }
    });
    return this;
  }

  @Override
  public BlockBuilder<T> _(final Statement stmt) {
    return append(stmt);
  }

  @Override
  public BlockBuilder<T> _(final InnerClass innerClass) {
    return append(innerClass);
  }

  @Override
  public T finish() {
    if (callback != null) {
      return callback.callback(blockStatement);
    }
    return null;
  }
}