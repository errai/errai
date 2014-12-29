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

import java.lang.annotation.Annotation;

import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaConstructor;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.injector.Injector;

public class DecoratorTask extends InjectionTask {
  private final IOCDecoratorExtension[] iocExtensions;
  private final Class<? extends Annotation> annotationType;

  public DecoratorTask(final Injector injector, final MetaClass type, Class<? extends Annotation> annotationType,
      final IOCDecoratorExtension[] decs) {
    super(injector, type);
    this.iocExtensions = decs;
    this.annotationType = annotationType;
  }

  public DecoratorTask(final Injector injector, final MetaField field, Class<? extends Annotation> annotationType,
      final IOCDecoratorExtension[] decs) {
    super(injector, field);
    this.iocExtensions = decs;
    this.annotationType = annotationType;
  }

  public DecoratorTask(final Injector injector, final MetaConstructor constr,
      Class<? extends Annotation> annotationType,
      final IOCDecoratorExtension[] decs) {
    super(injector, constr);
    this.iocExtensions = decs;
    this.annotationType = annotationType;

  }

  public DecoratorTask(final Injector injector, final MetaMethod method, Class<? extends Annotation> annotationType,
      final IOCDecoratorExtension[] decs) {
    super(injector, method);
    this.iocExtensions = decs;
    this.annotationType = annotationType;
  }

  public DecoratorTask(final Injector injector, final MetaParameter parm, Class<? extends Annotation> annotationType,
      final IOCDecoratorExtension[] decs) {
    super(injector, parm);
    this.iocExtensions = decs;
    this.annotationType = annotationType;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public boolean doTask(final InjectionContext ctx) {
    Annotation annotation = null;

    for (final IOCDecoratorExtension<? extends Annotation> dec : iocExtensions) {
      switch (taskType) {

        case PrivateField:
        case Field:
          annotation = field.getAnnotation(annotationType);
          break;
        case PrivateMethod:
        case Method:
          annotation = method.getAnnotation(annotationType);
          if (annotation == null && field != null) {
            annotation = field.getAnnotation(annotationType);
          }
          else if (annotation == null && parm != null) {
            annotation = parm.getAnnotation(annotationType);
          }
          break;
        case Type:
          annotation = type.getAnnotation(annotationType);
          break;
        case Parameter:
          annotation = parm.getAnnotation(annotationType);
          break;
      }

      final InjectableInstance instance =
          new InjectableInstance(annotation, taskType, constructor, method, field, type, parm, injector, ctx);
      for (final Object stmt : dec.generateDecorator(instance)) {
        ctx.getProcessingContext().append((Statement) stmt);
      }
      injector.updateProxies();
    }
    return true;
  }
}
