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

package org.jboss.errai.codegen.framework.meta.impl.gwt;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.meta.MetaField;
import org.jboss.errai.codegen.framework.meta.MetaType;

import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JGenericType;
import org.jboss.errai.codegen.framework.util.GenUtil;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class GWTField extends MetaField {
  private JField field;
  private Annotation[] annotations;

  GWTField(JField field) {
    this.field = field;

    try {
      Class<?> cls = Class.forName(field.getEnclosingType().getQualifiedSourceName(), false,
          Thread.currentThread().getContextClassLoader());

      Field fld;
      try {
        fld = cls.getDeclaredField(field.getName());
      }
      catch (NoSuchFieldException e) {
        try {
          fld = cls.getField(field.getName());
        }
        catch (NoSuchFieldException e2) {
          throw new RuntimeException("could not find field: "
              + field.getName() + "; in class: " + cls.getCanonicalName());
        }
      }
      annotations = fld.getAnnotations();

    }
    catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public MetaClass getType() {
    return MetaClassFactory.get(field.getType());
  }

  @Override
  public String getName() {
    return field.getName();
  }

  @Override
  public Annotation[] getAnnotations() {
    return annotations == null ? new Annotation[0] : annotations;
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annotation) {
    for (Annotation a : getAnnotations()) {
      if (a.annotationType().equals(annotation)) return (A) a;
    }
    return null;
  }

  @Override
  public boolean isAnnotationPresent(Class<? extends Annotation> annotation) {
    return getAnnotation(annotation) != null;
  }

  @Override
  public MetaType getGenericType() {
    JGenericType genericType = field.getType().isGenericType();
    if (genericType != null) {
      return new GWTGenericDeclaration(genericType);
    }
    return null;
  }


  @Override
  public MetaClass getDeclaringClass() {
    return MetaClassFactory.get(field.getEnclosingType());
  }

  @Override
  public boolean isAbstract() {
    return false;
  }

  @Override
  public boolean isPublic() {
    return field.isPublic();
  }

  @Override
  public boolean isPrivate() {
    return field.isPrivate();
  }

  @Override
  public boolean isProtected() {
    return field.isProtected();
  }

  @Override
  public boolean isFinal() {
    return field.isFinal();
  }

  @Override
  public boolean isStatic() {
    return field.isStatic();
  }

  @Override
  public boolean isTransient() {
    return field.isTransient();
  }

  @Override
  public boolean isVolatile() {
    return field.isVolatile();
  }

  @Override
  public boolean isSynthetic() {
    return false;
  }

  @Override
  public boolean isSynchronized() {
    return false;
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof MetaField && GenUtil.equals(this, (MetaField) o);
  }
}
