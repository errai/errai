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

package org.jboss.errai.codegen.meta;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public abstract class MetaField implements HasAnnotations, MetaClassMember {
  public abstract MetaClass getType();

  public abstract MetaType getGenericType();

  public abstract String getName();

  @Override
  public abstract Annotation[] getAnnotations();

  public String toString() {
    return MetaField.class.getName() + ":" + getDeclaringClass().getFullyQualifiedName() + "." + getName();
  }
  
  private String _hashString;
  public String hashString() {
    if (_hashString != null) return _hashString;
    return _hashString = MetaField.class.getName()
            + ":" + getDeclaringClass().getFullyQualifiedName() + "." + getName() + "::" + getType().getFullyQualifiedName();
  }

  public int hashCode() {
    return hashString().hashCode();
  }


  @Override
  public boolean equals(Object o) {
    return o instanceof MetaField && ((MetaField) o).hashString().equals(hashString());
  }


  public Field asField() {
    try {
      final Class<?> aClass = Class.forName(getDeclaringClass().getFullyQualifiedName());
      return aClass.getDeclaredField(getName());
    }
    catch (Throwable e) {
      return null;
    }
  }

  public static class ArrayLengthMetaField extends MetaField {

    private MetaClass componentType;

    public ArrayLengthMetaField(MetaClass componentType) {
      this.componentType = componentType;
    }

    @Override
    public MetaClass getType() {
      return MetaClassFactory.get(int.class);
    }

    @Override
    public MetaType getGenericType() {
      return null;
    }

    @Override
    public String getName() {
      return "length";
    }

    @Override
    public Annotation[] getAnnotations() {
      return new Annotation[0];
    }

    @Override
    public MetaClass getDeclaringClass() {
      return componentType;
    }

    @Override
    public boolean isAbstract() {
      return false;
    }

    @Override
    public boolean isPublic() {
      return true;
    }

    @Override
    public boolean isPrivate() {
      return false;
    }

    @Override
    public boolean isProtected() {
      return false;
    }

    @Override
    public boolean isFinal() {
      return false;
    }

    @Override
    public boolean isStatic() {
      return false;
    }

    @Override
    public boolean isTransient() {
      return false;
    }

    @Override
    public boolean isSynthetic() {
      return false;
    }

    @Override
    public boolean isVolatile() {
      return false;
    }

    @Override
    public boolean isSynchronized() {
      return false;
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotation) {
      return false;
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotation) {
      return null;
    }
  }
}
