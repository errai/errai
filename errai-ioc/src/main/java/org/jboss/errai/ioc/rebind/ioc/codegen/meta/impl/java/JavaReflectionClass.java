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

package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.java;

import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.*;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.AbstractMetaClass;

import javax.enterprise.util.TypeLiteral;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

public class JavaReflectionClass extends AbstractMetaClass<Class> {
  private Annotation[] annotationsCache;

  private JavaReflectionClass(Class clazz) {
    super(clazz);
    this.annotationsCache = clazz.getAnnotations();
    Type type = getEnclosedMetaObject().getGenericSuperclass();
    if (type instanceof ParameterizedType) {
      super.parameterizedType = new JavaReflectionParameterizedType((ParameterizedType) type);
    }
  }

  private JavaReflectionClass(TypeLiteral typeLiteral) {
    super(typeLiteral.getRawType());
    if (typeLiteral.getType() instanceof ParameterizedType) {
      super.parameterizedType = new JavaReflectionParameterizedType((ParameterizedType) typeLiteral.getType());
    }
  }

  public static MetaClass newInstance(Class type) {
    return MetaClassFactory.get(type);
  }

  public static MetaClass newUncachedInstance(Class type) {
    return new JavaReflectionClass(type);
  }

  public static MetaClass newInstance(TypeLiteral type) {
    return MetaClassFactory.get(type);
  }

  public static MetaClass newUncachedInstance(TypeLiteral type) {
    return new JavaReflectionClass(type);
  }

  public String getName() {
    return getEnclosedMetaObject().getSimpleName();
  }

  public String getFullyQualifedName() {
    return getCanonicalName();
  }

  @Override
  public String getCanonicalName() {
    return getEnclosedMetaObject().getCanonicalName();
  }

  @Override
  public String getInternalName() {
    return getEnclosedMetaObject().getName();
  }

  private static MetaMethod[] fromMethodArray(Method[] methods) {
    List<MetaMethod> methodList = new ArrayList<MetaMethod>();

    for (Method m : methods) {
      methodList.add(new JavaReflectionMethod(m));
    }

    return methodList.toArray(new MetaMethod[methodList.size()]);
  }

  public MetaMethod[] getMethods() {
    return fromMethodArray(getEnclosedMetaObject().getMethods());
  }

  public MetaMethod[] getDeclaredMethods() {
    return fromMethodArray(getEnclosedMetaObject().getDeclaredMethods());
  }

  private static MetaField[] fromFieldArray(Field[] methods) {
    List<MetaField> methodList = new ArrayList<MetaField>();

    for (Field f : methods) {
      methodList.add(new JavaReflectionField(f));
    }

    return methodList.toArray(new MetaField[methodList.size()]);
  }

  public MetaField[] getFields() {
    return fromFieldArray(getEnclosedMetaObject().getFields());
  }

  public MetaField[] getDeclaredFields() {
    return fromFieldArray(getEnclosedMetaObject().getDeclaredFields());
  }

  public MetaField getField(String name) {
    try {
      return new JavaReflectionField(getEnclosedMetaObject().getField(name));
    } catch (Exception e) {
      throw new RuntimeException("Could not get field: " + name, e);
    }
  }

  public MetaField getDeclaredField(String name) {
    try {
      return new JavaReflectionField(getEnclosedMetaObject().getDeclaredField(name));
    } catch (Exception e) {
      throw new RuntimeException("Could not get field: " + name, e);
    }
  }

  public MetaConstructor[] getConstructors() {
    List<MetaConstructor> constructorList = new ArrayList<MetaConstructor>();

    for (Constructor c : getEnclosedMetaObject().getConstructors()) {
      constructorList.add(new JavaReflectionConstructor(c));
    }

    return constructorList.toArray(new MetaConstructor[constructorList.size()]);
  }

  public MetaConstructor[] getDeclaredConstructors() {
    List<MetaConstructor> constructorList = new ArrayList<MetaConstructor>();

    for (Constructor c : getEnclosedMetaObject().getDeclaredConstructors()) {
      constructorList.add(new JavaReflectionConstructor(c));
    }

    return constructorList.toArray(new MetaConstructor[constructorList.size()]);
  }

  public MetaConstructor getConstructor(Class... parameters) {
    try {
      return new JavaReflectionConstructor(getEnclosedMetaObject().getConstructor(parameters));
    } catch (Exception e) {
      throw new RuntimeException("Could not get constructor", e);
    }
  }

  public MetaConstructor getDeclaredConstructor(Class... parameters) {
    try {
      return new JavaReflectionConstructor(getEnclosedMetaObject().getDeclaredConstructor(parameters));
    } catch (Exception e) {
      throw new RuntimeException("Could not get constructor", e);
    }
  }

  public MetaClass[] getInterfaces() {
    List<MetaClass> metaClassList = new ArrayList<MetaClass>();
    for (Class<?> type : getEnclosedMetaObject().getInterfaces()) {

      metaClassList.add(new JavaReflectionClass(type));
    }

    return metaClassList.toArray(new MetaClass[metaClassList.size()]);
  }

  public MetaClass getSuperClass() {
    return MetaClassFactory.get(getEnclosedMetaObject().getSuperclass());
  }

  public MetaClass getComponentType() {
    return MetaClassFactory.get(getEnclosedMetaObject().getComponentType());
  }

  public Annotation[] getAnnotations() {
    if (annotationsCache == null) {
      annotationsCache = getEnclosedMetaObject().getAnnotations();
    }
    return annotationsCache;
  }

  public MetaTypeVariable[] getTypeParameters() {
    return JavaReflectionUtil.fromTypeVariable(getEnclosedMetaObject().getTypeParameters());
  }

  public boolean isInterface() {
    return getEnclosedMetaObject().isInterface();
  }

  public boolean isAbstract() {
    return (getEnclosedMetaObject().getModifiers() & Modifier.ABSTRACT) != 0;
  }

  public boolean isArray() {
    return getEnclosedMetaObject().isArray();
  }

  public boolean isEnum() {
    return getEnclosedMetaObject().isEnum();
  }

  public boolean isAnnotation() {
    return getEnclosedMetaObject().isAnnotation();
  }

  public boolean isPublic() {
    return (getEnclosedMetaObject().getModifiers() & Modifier.PUBLIC) != 0;
  }

  public boolean isPrivate() {
    return (getEnclosedMetaObject().getModifiers() & Modifier.PRIVATE) != 0;
  }

  public boolean isProtected() {
    return (getEnclosedMetaObject().getModifiers() & Modifier.PROTECTED) != 0;
  }

  public boolean isFinal() {
    return (getEnclosedMetaObject().getModifiers() & Modifier.FINAL) != 0;
  }

  public boolean isStatic() {
    return (getEnclosedMetaObject().getModifiers() & Modifier.STATIC) != 0;
  }

  public String toString() {
    return getFullyQualifedName();
  }

}
