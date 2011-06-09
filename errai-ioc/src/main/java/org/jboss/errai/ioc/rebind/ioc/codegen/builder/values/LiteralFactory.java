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

package org.jboss.errai.ioc.rebind.ioc.codegen.builder.values;


import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.LoadClassReference;

/**
 * The literal factory provides a LiteralValue for
 * the specified object (if possible).
 *
 * @author Mike Brock <cbrock@redhat.com>
 */
public class LiteralFactory {
  public static LiteralValue<?> getLiteral(final Context context, final Object o) {
    if (o instanceof Class) {
      return new LiteralValue<Class>((Class<?>) o) {
        @Override
        public String getCanonicalString(Context context) {
          return LoadClassReference.getClassReference(MetaClassFactory.get((Class<?>) o), context);
        }
      };
    }
    else {
      return getLiteral(o);
    }
  }

  public static LiteralValue<?> getLiteral(Object o) {
    if (o == null) {
      return NullLiteral.INSTANCE;
    }

    if (o instanceof String) {
      return new StringLiteral((String) o);
    }
    else if (o instanceof Integer) {
      return new IntValue((Integer) o);
    }
    else if (o instanceof Boolean) {
      return new BooleanValue((Boolean) o);
    }
    else if (o instanceof Short) {
      return new ShortValue((Short) o);
    }
    else if (o instanceof Long) {
      return new LongValue((Long) o);
    }
    else if (o instanceof Double) {
      return new DoubleValue((Double) o);
    }
    else if (o instanceof Float) {
      return new FloatValue((Float) o);
    }
    else if (o instanceof Byte) {
      return new ByteValue((Byte) o);
    }
    else if (o instanceof Class) {
      return new ClassLiteral((Class) o);
    }
    else if (o.getClass().isArray()) {
      return new ArrayLiteral(o);
    }
    else {
      throw new IllegalArgumentException("type cannot be converted to a literal: "
          + o.getClass().getName());
    }
  }
}
