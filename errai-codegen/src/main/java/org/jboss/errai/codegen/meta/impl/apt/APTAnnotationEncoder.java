/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.codegen.meta.impl.apt;

import org.jboss.errai.codegen.Context;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.util.Stmt;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.jboss.errai.codegen.util.PrettyPrinter.prettyPrintJava;

public class APTAnnotationEncoder {
  public static Statement encode(final APTAnnotation metaAnnotation) {
    final MetaClass annotationMetaClass = metaAnnotation.annotationType();

    return new Statement() {
      String generatedCache;

      @Override
      public String generate(final Context context) {

        if (generatedCache != null) {
          return generatedCache;
        }

        final AnonymousClassStructureBuilder builder = ObjectBuilder.newInstanceOf(annotationMetaClass, context)
                .extend();

        final List<MetaMethod> sortedMethods = Arrays.stream(annotationMetaClass.getMethods())
                .sorted(Comparator.comparing(MetaMethod::getName))
                .filter(m -> !m.getDeclaringClassName().equals(Object.class.getName()))
                .filter(m -> !m.getName().equals("equals") && !m.getName().equals("hashCode"))
                .collect(toList());

        for (final MetaMethod method : sortedMethods) {
          builder.publicOverridesMethod(method.getName())
                  .append(Stmt.load(convertValue(annotationMetaClass, method)).returnValue())
                  .finish();
        }

        return generatedCache = prettyPrintJava(builder.finish().toJavaString());
      }

      private Object convertValue(final MetaClass annotationMetaClass, final MetaMethod method) {
        if (method.getName().equals("annotationType")) {
          return annotationMetaClass;
        } else if (method.getName().equals("toString")) {
          return annotationMetaClass.toString();
        } else {
          final Object value = metaAnnotation.value(method.getName(), method.getReturnType());
          return Stmt.load(value);
        }
      }

      @Override
      public MetaClass getType() {
        return annotationMetaClass;
      }
    };
  }
}
