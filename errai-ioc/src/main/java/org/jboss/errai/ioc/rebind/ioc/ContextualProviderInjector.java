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

package org.jboss.errai.ioc.rebind.ioc;

import org.jboss.errai.ioc.rebind.IOCGenerator;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.*;

import java.lang.annotation.Annotation;

public class ContextualProviderInjector extends TypeInjector {
  private final Injector providerInjector;

  public ContextualProviderInjector(MetaClass type, MetaClass providerType) {
    super(type);
    this.providerInjector = new TypeInjector(providerType);
  }

  @Override
  public String getType(InjectionContext injectContext, InjectionPoint injectionPoint) {
    injected = true;

    MetaClass type = null;
    MetaParameterizedType pType = null;

    switch (injectionPoint.getTaskType()) {
      case PrivateField:
      case Field:
        MetaField field = injectionPoint.getField();
        type = field.getType();

        pType = type.getParameterizedType();

        break;

      case Parameter:
        MetaParameter parm = injectionPoint.getParm();
        type = parm.getType();

        pType = type.getParameterizedType();
        break;
    }

    StringBuilder sb = new StringBuilder();

    if (pType == null) {
      sb.append(providerInjector.getType(injectContext, injectionPoint)).append(".provide(new Class[] {}");
    } else {
      MetaType[] typeArgs = pType.getTypeParameters();
      sb.append("(").append(type.getFullyQualifedName()).append("<")
              .append(typeArgs[0].toString()).append(">) ");

      sb.append(providerInjector.getType(injectContext, injectionPoint)).append(".provide(new Class[] {");
      for (int i = 0; i < typeArgs.length; i++) {
        sb.append(typeArgs[i].toString()).append(".class");

        if ((i + 1) < typeArgs.length) {
          sb.append(", ");
        }
      }
      sb.append("}");

      Annotation[] qualifiers = injectionPoint.getQualifiers();
      if (qualifiers.length != 0) {
        sb.append(", new java.lang.annotation.Annotation[] {");
        for (int i = 0; i < qualifiers.length; i++) {
          sb.append("\nnew java.lang.annotation.Annotation() {")
                  .append("\npublic Class<? extends java.lang.annotation.Annotation> annotationType() {\n return ")
                  .append(qualifiers[i].annotationType().getName()).append(".class").append(";\n}\n}");
          if ((i + 1) < qualifiers.length) sb.append(",");
        }
        sb.append("\n}");
      } else {
        sb.append(", null");
      }
    }
    sb.append(")");

    return IOCGenerator.debugOutput(sb);
  }

  @Override
  public String instantiateOnly(InjectionContext injectContext, InjectionPoint injectionPoint) {
    injected = true;
    return providerInjector.getType(injectContext, injectionPoint);
  }
}
