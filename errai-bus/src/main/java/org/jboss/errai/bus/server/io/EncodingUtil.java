/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.bus.server.io;

import org.mvel2.util.PropertyTools;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class EncodingUtil {
  private static Field[] __getAllEncodingFields(Class cls) {
    List<Field[]> heirarchy = new ArrayList<Field[]>();

    do {
      heirarchy.add(cls.getDeclaredFields());
    } while ((cls = cls.getSuperclass()) != Object.class);

    List<Field> encodingFields = new ArrayList<Field>();

    for (Field[] fls : heirarchy) {
      for (Field f : fls) {
        if (isSerializable(f)) {
          f.setAccessible(true);
          encodingFields.add(f);
        }
      }
    }

    return encodingFields.toArray(new Field[encodingFields.size()]);
  }

  public static boolean isSerializable(Field f) {
    return !f.isSynthetic() && (f.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT)) == 0;
  }

  public static Field[] getAllEncodingFields(final Class cls) {
    return EncodingCache.get(cls, new EncodingCache.ValueProvider<Field[]>() {
      public Field[] get() {
        return __getAllEncodingFields(cls);
      }
    });
  }
}
