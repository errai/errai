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

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.*;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.LoadClassReference;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClassFactory;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class FieldBuilder<T> implements FieldBuildStart<T>, FieldBuildType<T>,
        FieldBuildName<T>, FieldBuildInitializer<T> {
  private BuildCallback<T> callback;
  private Scope scope;
  private MetaClass type;
  private String name;
  private Statement initializer;

  public FieldBuilder(BuildCallback<T> callback) {
    this.callback = callback;
  }

  public FieldBuilder(BuildCallback<T> callback, Scope scope) {
    this.callback = callback;
    this.scope = scope;
  }

  public FieldBuilder(BuildCallback<T> callback, Scope scope, MetaClass type, String name) {
    this.callback = callback;
    this.scope = scope;
    this.type = type;
    this.name = name;
  }

  public Finishable<T> initializesWith(Statement statement) {
    this.initializer = statement;
    return this;
  }

  public FieldBuildType<T> publicScope() {
    scope = Scope.Public;
    return this;
  }

  public FieldBuildType<T> privateScope() {
    scope = Scope.Private;
    return this;
  }

  public FieldBuildType<T> protectedScope() {
    scope = Scope.Protected;
    return this;
  }

  public FieldBuildType<T> packageScope() {
    scope = Scope.Package;
    return this;
  }

  public FieldBuildName<T> typeOf(Class<?> type) {
    this.type = MetaClassFactory.get(type);
    return this;
  }

  public FieldBuildName<T> typeOf(MetaClass type) {
    this.type = type;
    return this;
  }

  public FieldBuildInitializer<T> named(String name) {
    this.name = name;
    return this;
  }

  public T finish() {
    if (callback != null) {
      return callback.callback(new Statement() {
        public String generate(Context context) {
          StringBuilder sbuf = new StringBuilder(scope.getCanonicalName())
                  .append(scope == Scope.Package ? "" : " ")
                  .append(LoadClassReference.getClassReference(type, context))
                  .append(" ").append(name);

          if (initializer != null) {
            sbuf.append(" = ").append(initializer.generate(context));
          }

          return sbuf.append(";").toString();
        }

        public MetaClass getType() {
          return type;
        }

        public Context getContext() {
          return null;
        }
      });
    }
    return null;
  }
}
