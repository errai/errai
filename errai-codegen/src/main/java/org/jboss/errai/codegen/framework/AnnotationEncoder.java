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

package org.jboss.errai.codegen.framework;

import org.jboss.errai.codegen.framework.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.framework.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.util.Stmt;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.jboss.errai.codegen.framework.util.PrettyPrinter.prettyPrintJava;

public class AnnotationEncoder {
  public static Statement encode(final Annotation annotation) {
    final Class<? extends Annotation> annotationClass = annotation.annotationType();

    return new Statement() {
      @Override
      public String generate(Context context) {
        final AnonymousClassStructureBuilder builder
                = ObjectBuilder.newInstanceOf(annotationClass, context)
                .extend();

        Class<? extends Annotation> annoClass = annotation.getClass();

        for (Method method : annoClass.getDeclaredMethods()) {
          if (((method.getModifiers() & (Modifier.PRIVATE | Modifier.PROTECTED)) == 0)
                  && (!"equals".equals(method.getName()) && !"hashCode".equals(method.getName()))) {
            try {
              builder.publicOverridesMethod(method.getName())
                      .append(Stmt.load(method.invoke(annotation)).returnValue()).finish();
            }
            catch (IllegalAccessException e) {
              throw new RuntimeException("error generation annotation wrapper", e);
            }
            catch (InvocationTargetException e) {
              throw new RuntimeException("error generation annotation wrapper", e);
            }
          }
        }

        return prettyPrintJava(builder.finish().toJavaString());
      }

      @Override
      public MetaClass getType() {
        return MetaClassFactory.get(annotationClass);
      }
    };
  }

  public static Statement[] encode(Annotation[] annotations) {
    Statement[] statements = new Statement[annotations.length];
    for (int i = 0; i < annotations.length; i++) {
      statements[i] = encode(annotations[i]);
    }
    return statements;
  }
}
