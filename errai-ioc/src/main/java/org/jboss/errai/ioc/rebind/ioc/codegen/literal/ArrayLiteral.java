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

package org.jboss.errai.ioc.rebind.ioc.codegen.literal;

import java.lang.reflect.Array;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.LoadClassReference;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.util.GenUtil;

/**
 * Renders an array back to it's canonical Java-based literal representation, assuming the contents
 * of the array can be represented as such.
 *
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ArrayLiteral extends LiteralValue<Object> {
  private final int dimensions;
  private Class<?> arrayType;

  public ArrayLiteral(Object value) {
    super(value);

    Class<?> type = value.getClass();
    int dim = 0;
    while (type.isArray()) {
      dim++;
      type = type.getComponentType();
    }

    this.dimensions = dim;
    this.arrayType = type;
  }

  @Override
  public String getCanonicalString(Context context) {
    StringBuilder buf = new StringBuilder("new " +
            LoadClassReference.getClassReference(MetaClassFactory.get(arrayType), context));

    Object val = getValue();

    if (Array.getLength(val) == 0) {
      return buf.append("[0]").toString();
    }

    for (int i = 0; i < dimensions; i++) {
      buf.append("[]");
    }
    buf.append(" ");
    buf.append(renderInlineArrayLiteral(context, val));

    return buf.toString();
  }

  private static String renderInlineArrayLiteral(Context context, Object arrayInstance) {
    StringBuilder builder = new StringBuilder("{ ");

    int length = Array.getLength(arrayInstance);

    Object element;

    for (int i = 0; i < length; i++) {
      element = Array.get(arrayInstance, i);
      if (element.getClass().isArray()) {
        builder.append(renderInlineArrayLiteral(context, element));

      }
      else {
        builder.append(GenUtil.generate(context, element).generate(context));
      }

      if (i + 1 < length) {
        builder.append(", ");
      }
    }

    return builder.append(" }").toString();
  }
}
