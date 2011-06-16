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

package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.gwt;

import com.google.gwt.core.ext.typeinfo.JAbstractMethod;
import com.google.gwt.core.ext.typeinfo.JParameter;
import org.jboss.errai.ioc.rebind.ioc.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class GWTParameter implements MetaParameter {
  private JParameter parameter;
  private Annotation[] annotations;
  private MetaClassMember declaredBy;

  GWTParameter(JParameter parameter, MetaMethod declaredBy) {
    this.parameter = parameter;
    this.declaredBy = declaredBy;

    try {
      Class<?> cls = Class.forName(parameter.getEnclosingMethod().getEnclosingType().getQualifiedSourceName(),
          false, Thread.currentThread().getContextClassLoader());

      JAbstractMethod jMethod = parameter.getEnclosingMethod();

      int index = -1;
      for (int i = 0; i < jMethod.getParameters().length; i++) {
        if (jMethod.getParameters()[i].getName().equals(parameter.getName())) {
          index = i;
        }
      }

      Method method = null;
      try {
        method = cls.getDeclaredMethod(jMethod.getName(),
            InjectUtil.jParmToClass(jMethod.getParameters()));
      }
      catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
      catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }

      annotations = method.getParameterAnnotations()[index];

    }
    catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  GWTParameter(JParameter parameter, MetaConstructor declaredBy) {
    this.parameter = parameter;
    this.declaredBy = declaredBy;

    try {
      Class<?> cls = Class.forName(parameter.getEnclosingMethod().getEnclosingType().getQualifiedSourceName(),
          false, Thread.currentThread().getContextClassLoader());

      JAbstractMethod jMethod = parameter.getEnclosingMethod();

      int index = -1;
      for (int i = 0; i < jMethod.getParameters().length; i++) {
        if (jMethod.getParameters()[i].getName().equals(parameter.getName())) {
          index = i;
        }
      }

      Constructor c = null;
      try {
        c = cls.getConstructor(InjectUtil.jParmToClass(jMethod.getParameters()));
      }
      catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      }

      annotations = c.getParameterAnnotations()[index];

    }
    catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  public String getName() {
    return parameter.getName();
  }

  public MetaClass getType() {
    return MetaClassFactory.get(parameter.getType());
  }

  public Annotation[] getAnnotations() {
    return annotations == null ? new Annotation[0] : annotations;
  }

  public final <A extends Annotation> A getAnnotation(Class<A> annotation) {
    for (Annotation a : getAnnotations()) {
      if (a.annotationType().equals(annotation)) return (A) a;
    }
    return null;
  }

  public final boolean isAnnotationPresent(Class<? extends Annotation> annotation) {
    return getAnnotation(annotation) != null;
  }

  public MetaClassMember getDeclaringMember() {
    return declaredBy;
  }
}
