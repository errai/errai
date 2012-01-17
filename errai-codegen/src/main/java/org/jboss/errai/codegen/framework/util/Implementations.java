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

package org.jboss.errai.codegen.framework.util;

import org.jboss.errai.codegen.framework.Context;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.StringStatement;
import org.jboss.errai.codegen.framework.Variable;
import org.jboss.errai.codegen.framework.builder.BlockBuilder;
import org.jboss.errai.codegen.framework.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.framework.builder.ContextualStatementBuilder;
import org.jboss.errai.codegen.framework.builder.StatementEnd;
import org.jboss.errai.codegen.framework.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class Implementations {
  public static ClassStructureBuilder<?> implement(Class<?> clazz) {
    return ClassBuilder.define(clazz.getPackage().getName() + "." + clazz.getSimpleName() + "Impl")
            .publicScope()
            .implementsInterface(clazz)
            .body();
  }

  public static ClassStructureBuilder<?> implement(Class<?> clazz, String implClassName) {
    return ClassBuilder.define(clazz.getPackage().getName() + "." + implClassName)
            .publicScope()
            .implementsInterface(clazz)
            .body();
  }

  public static ClassStructureBuilder<?> implement(Class<?> clazz, String implPackageName, String implClassName) {
    return ClassBuilder.define(implPackageName + "." + implClassName)
            .publicScope()
            .implementsInterface(clazz)
            .body();
  }


  public static void autoInitializedField(ClassStructureBuilder<?> builder, MetaClass type,
                                          String name, Class<?> implementation) {

    autoInitializedField(builder, type, name, MetaClassFactory.get(implementation));
  }

  public static void autoInitializedField(ClassStructureBuilder<?> builder, MetaClass type,
                                          String name, MetaClass implementation) {

    implementation = MetaClassFactory.parameterizedAs(implementation, type.getParameterizedType());

    builder.privateField(name, type)
            .initializesWith(Stmt.newObject(implementation)).finish();
  }

  public static StringBuilderBuilder newStringBuilder() {
    return newStringBuilder(64);
  }
  
  public static StringBuilderBuilder newStringBuilder(int length) {
    final ContextualStatementBuilder statementBuilder
            = Stmt.nestedCall(Stmt.newObject(StringBuilder.class).withParameters(length));

    return new StringBuilderBuilder() {

      @Override
      public StringBuilderBuilder append(Object statement) {
        statementBuilder.invoke("append", statement);
        return this;
      }

      String generatedCache;

      @Override
      public String generate(Context context) {
        if (generatedCache != null) return generatedCache;
        return generatedCache = statementBuilder.generate(context);
      }

      @Override
      public MetaClass getType() {
        return statementBuilder.getType();
      }
    };
  }


  public static interface StringBuilderBuilder extends Statement {
    public StringBuilderBuilder append(Object statement);
  }

  public static BlockBuilder<StatementEnd> autoForLoop(String varName, Statement value) {
    return Stmt.for_(Stmt.declareVariable(int.class).named("i").initializeWith(0),
            Bool.lessThan(Variable.get("i"), value),
            new StringStatement(varName + "++"));
  }

}
