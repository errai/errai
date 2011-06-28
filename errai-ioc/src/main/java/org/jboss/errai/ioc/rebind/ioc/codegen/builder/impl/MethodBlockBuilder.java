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
import org.jboss.errai.ioc.rebind.ioc.codegen.ThrowsDeclaration;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.MethodBuildCallback;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class MethodBlockBuilder<T> extends BlockBuilder<T> {
  private ThrowsDeclaration throwsDeclaration = ThrowsDeclaration.none();
  private MethodBuildCallback<T> callback;

  public MethodBlockBuilder(MethodBuildCallback<T> callback) {
    this.callback = callback;
  }
  
  public MethodBlockBuilder(BlockStatement blockStatement, MethodBuildCallback<T> callback) {
    this.blockStatement = blockStatement;
    this.callback = callback;
  }

  public BlockBuilder<T> throws_(Class<? extends Throwable>... exceptionTypes) {
    throwsDeclaration = ThrowsDeclaration.of(exceptionTypes);
    return this;
  }

  public BlockBuilder<T> throws_(MetaClass... exceptions) {
    throwsDeclaration = ThrowsDeclaration.of(exceptions);
    return this;
  }
  
  @Override
  public T finish() {
    if (callback != null) {
      return callback.callback(blockStatement, throwsDeclaration);
    }
    return null;
  }
}
