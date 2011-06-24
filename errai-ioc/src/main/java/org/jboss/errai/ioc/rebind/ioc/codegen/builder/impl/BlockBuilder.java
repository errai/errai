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

import org.jboss.errai.ioc.rebind.ioc.codegen.BlockStatement;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.BuildCallback;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.Finishable;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class BlockBuilder<T> implements Finishable<T> {
  protected BlockStatement blockStatement;
  protected BuildCallback<T> callback;

  public BlockBuilder() {
    this.blockStatement = new BlockStatement();
  }

  public BlockBuilder(BuildCallback<T> callback) {
    this();
    this.callback = callback;
  }

  public BlockBuilder(BlockStatement blockStatement, BuildCallback<T> callback) {
    this.blockStatement = blockStatement;
    this.callback = callback;
  }

  public BlockBuilder<T> append(Statement statement) {
    blockStatement.addStatement(statement);
    return this;
  }

  public T finish() {
    if (callback != null) {
      return callback.callback(blockStatement);
    }
    return null;
  }
}
