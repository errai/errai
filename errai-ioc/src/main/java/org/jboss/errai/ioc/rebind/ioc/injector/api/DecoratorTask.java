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
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.injector.Injector;

import java.lang.annotation.Annotation;

public class DecoratorTask extends InjectionTask {
  private final IOCDecoratorExtension[] IOCExtensions;

  public DecoratorTask(Injector injector, MetaClass type, IOCDecoratorExtension[] decs) {
    super(injector, type);
    this.IOCExtensions = decs;
  }

  public DecoratorTask(Injector injector, MetaField field, IOCDecoratorExtension[] decs) {
    super(injector, field);
    this.IOCExtensions = decs;
  }

  public DecoratorTask(Injector injector, MetaMethod method, IOCDecoratorExtension[] decs) {
    super(injector, method);
    this.IOCExtensions = decs;
  }

  public DecoratorTask(Injector injector, MetaParameter parm, IOCDecoratorExtension[] decs) {
    super(injector, parm);
    this.IOCExtensions = decs;
  }

  @SuppressWarnings({"unchecked"})
  @Override
  public boolean doTask(InjectionContext ctx) {
    Annotation anno = null;

    for (IOCDecoratorExtension<? extends Annotation> dec : IOCExtensions) {
      switch (taskType) {
        case PrivateField:
        case Field:
          anno = field.getAnnotation(dec.decoratesWith());
          break;
        case PrivateMethod:
        case Method:
          anno = method.getAnnotation(dec.decoratesWith());
          if (anno == null && field != null) {
            anno = field.getAnnotation(dec.decoratesWith());
          }
          else if (anno == null && parm != null) {
            anno = parm.getAnnotation(dec.decoratesWith());
          }
          break;
        case Type:
          anno = type.getAnnotation(dec.decoratesWith());
          break;

      }

      for (Statement stmt : dec.generateDecorator(new InjectableInstance(anno, taskType, constructor, method, field, type,
              parm, injector, ctx))) {
        ctx.getProcessingContext().append(stmt);
      }
    }
    return true;
  }
}
