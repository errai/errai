/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ioc.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;

import java.util.Collection;

/**
 * @author Mike Brock
 */
public final class RunAsyncWrapper {
  private RunAsyncWrapper() {
  }

  public static Statement wrap(final Statement statement) {
    return Stmt.invokeStatic(GWT.class, "runAsync", ObjectBuilder.newInstanceOf(RunAsyncCallback.class).extend()
        .publicOverridesMethod("onFailure", Parameter.of(Throwable.class, "throwable"))
        .append(Stmt.throw_(RuntimeException.class, "failed to run asynchronously", Refs.get("throwable")))
        .finish()
        .publicOverridesMethod("onSuccess")
        .append(statement).finish().finish());
  }

  public static Statement wrap(final Collection<Statement> statements) {
    final BlockBuilder<AnonymousClassStructureBuilder> bb = ObjectBuilder.newInstanceOf(RunAsyncCallback.class).extend()
        .publicOverridesMethod("onFailure", Parameter.of(Throwable.class, "throwable"))
        .append(Stmt.throw_(RuntimeException.class, "failed to run asynchronously", Refs.get("throwable")))
        .finish()
        .publicOverridesMethod("onSuccess");

    for (Statement stmt : statements) {
      bb.append(stmt);
    }
    return Stmt.invokeStatic(GWT.class, "runAsync", bb.finish().finish());

  }
}
