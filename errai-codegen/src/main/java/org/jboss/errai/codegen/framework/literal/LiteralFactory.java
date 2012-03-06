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

package org.jboss.errai.codegen.framework.literal;

import static org.jboss.errai.codegen.framework.builder.callstack.LoadClassReference.getClassReference;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import org.jboss.errai.codegen.framework.AnnotationEncoder;
import org.jboss.errai.codegen.framework.Context;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.meta.MetaType;

/**
 * The literal factory provides a LiteralValue for the specified object (if possible).
 *
 * @author Mike Brock <cbrock@redhat.com>
 */
public class LiteralFactory {

  private static Map<Object, LiteralValue<?>> LITERAL_CACHE = new HashMap<Object, LiteralValue<?>>();

  public static LiteralValue<?> getLiteral(final Object o) {
    LiteralValue<?> result = LITERAL_CACHE.get(o);
    if (result == null) {


      if (o instanceof MetaType) {
        result = new LiteralValue<MetaType>((MetaType) o) {
          @Override
          public String getCanonicalString(Context context) {
            return getClassReference((MetaClass) o, context, false) + ".class";
          }
          
          public String toString() {
            return o.toString() + ".class";
          }
        };
      }
      else if (o instanceof Annotation) {
        result = new LiteralValue<Annotation>((Annotation) o) {
          @Override
          public String getCanonicalString(Context context) {
            return AnnotationEncoder.encode((Annotation) o).generate(context);
          }
        };
      }
      else if (o instanceof Enum) {
        result = new LiteralValue<Enum>((Enum) o) {
          @Override
          public String getCanonicalString(Context context) {
            return getClassReference(MetaClassFactory.get(o.getClass()), context) + "." + ((Enum) o).name();
          }
        };
      }
      else {
        result = _getLiteral(o);
      }
      LITERAL_CACHE.put(o, result);
    }
    return result;
  }

  public static LiteralValue<?> _getLiteral(Object o) {
    if (o == null) {
      return NullLiteral.INSTANCE;
    }

    if (o instanceof String) {
      return new StringLiteral((String) o);
    }
    else if (o instanceof Integer) {
      return new IntValue((Integer) o);
    }
    else if (o instanceof Character) {
      return new CharValue((Character) o);
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
    else if (o instanceof MetaClass) {
      return new MetaClassLiteral((MetaClass) o);
    }
    else if (o.getClass().isArray()) {
      return new ArrayLiteral(o);
    }
    else {
      throw new IllegalArgumentException("type cannot be converted to a literal: "
              + o.getClass().getName());
    }
  }

  public static LiteralValue<?> isLiteral(Object o) {
    try {
      return getLiteral(o);
    }
    catch (IllegalArgumentException a) {
      return null;
    }
  }
}