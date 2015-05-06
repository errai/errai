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

package org.jboss.errai.ioc.rebind.ioc.injector.api;


import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaConstructor;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.injector.Injector;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;

public class AsyncDecoratorTask extends AsyncInjectionTask {
  private final IOCDecoratorExtension[] IOCExtensions;

  public AsyncDecoratorTask(final Injector injector, final MetaClass type, final IOCDecoratorExtension[] decs) {
    super(injector, type);
    this.IOCExtensions = decs;
  }

  public AsyncDecoratorTask(final Injector injector, final MetaField field, final IOCDecoratorExtension[] decs) {
    super(injector, field);
    this.IOCExtensions = decs;
  }

  public AsyncDecoratorTask(final Injector injector, final MetaConstructor constr, final IOCDecoratorExtension[] decs) {
    super(injector, constr);
    this.IOCExtensions = decs;
  }

  public AsyncDecoratorTask(final Injector injector, final MetaMethod method, final IOCDecoratorExtension[] decs) {
    super(injector, method);
    this.IOCExtensions = decs;
  }

  public AsyncDecoratorTask(final Injector injector, final MetaParameter parm, final IOCDecoratorExtension[] decs) {
    super(injector, parm);
    this.IOCExtensions = decs;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public boolean doTask(final InjectionContext ctx) {
    Annotation annotation = null;

    for (final IOCDecoratorExtension<? extends Annotation> dec : IOCExtensions) {
      TaskSwitch:
      switch (taskType) {
        case PrivateField:
        case Field:
          for (final Annotation a : field.getAnnotations()) {
            if (ctx.isMetaAnnotationFor(a.annotationType(), dec.decoratesWith())) {
              annotation = field.getAnnotation(a.annotationType());
              break TaskSwitch;
            }
          }

          annotation = field.getAnnotation(dec.decoratesWith());
          break;
        case PrivateMethod:
        case Method:
          if (!ctx.getDecoratorAnnotationsBy(ElementType.METHOD).contains(dec.decoratesWith())) {
            for (final Annotation a : method.getAnnotations()) {
              if (ctx.isMetaAnnotationFor(a.annotationType(), dec.decoratesWith())) {
                annotation = method.getAnnotation(a.annotationType());
                break TaskSwitch;
              }
            }

            continue;
          }


          annotation = method.getAnnotation(dec.decoratesWith());
          if (annotation == null && field != null) {
            annotation = field.getAnnotation(dec.decoratesWith());
          }
          else if (annotation == null && parm != null) {
            annotation = parm.getAnnotation(dec.decoratesWith());
          }
          break;
        case Type:
          if (!ctx.getDecoratorAnnotationsBy(ElementType.TYPE).contains(dec.decoratesWith())) {
            for (final Annotation a : type.getAnnotations()) {
              if (ctx.isMetaAnnotationFor(a.annotationType(), dec.decoratesWith())) {
                annotation = type.getAnnotation(a.annotationType());
                break TaskSwitch;
              }
            }
            continue;
          }

          annotation = type.getAnnotation(dec.decoratesWith());
          break;
        case Parameter:
          if (!ctx.getDecoratorAnnotationsBy(ElementType.PARAMETER).contains(dec.decoratesWith())) {
            for (final Annotation a : parm.getAnnotations()) {
              if (ctx.isMetaAnnotationFor(a.annotationType(), dec.decoratesWith())) {
                annotation = parm.getAnnotation(a.annotationType());
                break TaskSwitch;
              }
            }
            continue;
          }

          annotation = parm.getAnnotation(dec.decoratesWith());
          break;
      }

      for (final Object stmt : dec.generateDecorator(
          new InjectableInstance(annotation, taskType, constructor, method, field, type, parm, injector, ctx))) {
        ctx.getProcessingContext().append((Statement)stmt);
      }
      injector.updateProxies();
    }
    return true;
  }
}
