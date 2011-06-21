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
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.ArrayBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.ArrayInitializationBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.LoadClassReference;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.util.GenUtil;

import java.lang.reflect.Array;

/**
 * StatementBuilder to create and initialize Arrays.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ArrayBuilderImpl extends AbstractStatementBuilder implements ArrayBuilder, ArrayInitializationBuilder {
  private MetaClass type;
  private MetaClass componentType;
  private Integer[] dimensions;
  private Object values = null;;

  protected ArrayBuilderImpl(Context context, CallElementBuilder callElementBuilder) {
    super(context, callElementBuilder);
  }

  public ArrayInitializationBuilder newArray(Class<?> componentType) {
    return newArray(componentType, new Integer[1]);
  }

  public ArrayInitializationBuilder newArray(Class<?> componentType, Integer... dimensions) {
    this.type = MetaClassFactory.get(Array.newInstance(componentType, 0).getClass());
    this.componentType = MetaClassFactory.get(componentType);
    this.dimensions = dimensions;
    return this;
  }

  public AbstractStatementBuilder initialize(Object... values) {
    if (values.length == 1 && values[0].getClass().isArray()
        && values.getClass().getComponentType().equals(Object.class)) {
      // this is a workaround for the jdt compiler which is coercing a multi-dimensional array
      // into the first element of our vararg instead of flattening it out (like javac does).
      this.values = values[0];
    } else {
      this.values = values;
    }
    
    return this;
  }

  public MetaClass getType() {
    return type;
  }

  public String generate(Context context) {
    StringBuilder buf = new StringBuilder();
    buf.append("new ").append(LoadClassReference.getClassReference(componentType, context));

    if (values != null) {
      generateWithInitialization(buf);
    }
    else {
      for (Integer dimension : dimensions) {
        if (dimension == null)
          throw new RuntimeException("Must provide either dimension expressions or an array initializer");

        buf.append("[").append(dimension).append("]");
      }
    }

    return buf.toString();
  }

  private void generateWithInitialization(StringBuilder buf) {
    int dim = 0;
    Class<?> type = values.getClass();
    while (type.isArray()) {
      dim++;
      type = type.getComponentType();
    }

    for (int i = 0; i < dim; i++) {
      buf.append("[]");
    }
    buf.append(" ");

    generateInitialization(buf, values);
  }

  private void generateInitialization(StringBuilder buf, Object values) {
    buf.append("{");
    int length = Array.getLength(values);
    for (int i = 0; i < length; i++) {
      Object element = Array.get(values, i);
      if (element.getClass().isArray()) {
        generateInitialization(buf, element);
      }
      else {
        Statement statement = GenUtil.generate(context, element);
        String statementExpr = statement.generate(context);
        GenUtil.assertAssignableTypes(statement.getType(), componentType);
        buf.append(statementExpr);
      }
      if (i + 1 < length) {
        buf.append(",");
      }
    }
    buf.append("}");
  }
}