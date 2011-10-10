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

package org.jboss.errai.codegen.framework.meta.impl.java;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.util.TypeLiteral;

import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.meta.MetaConstructor;
import org.jboss.errai.codegen.framework.meta.MetaField;
import org.jboss.errai.codegen.framework.meta.MetaMethod;
import org.jboss.errai.codegen.framework.meta.MetaTypeVariable;
import org.jboss.errai.codegen.framework.meta.impl.AbstractMetaClass;

public class JavaReflectionClass extends AbstractMetaClass<Class> {
  private Annotation[] annotationsCache;

  private JavaReflectionClass(Class clazz) {
    this(clazz, clazz.getGenericSuperclass());
  }

  private JavaReflectionClass(Class clazz, Type type) {
    super(clazz);
    this.annotationsCache = clazz.getAnnotations();
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

  public static MetaClass newUncachedInstance(Class clazz, Type type) {
    return new JavaReflectionClass(clazz, type);
  }

  public static MetaClass newInstance(TypeLiteral type) {
    return MetaClassFactory.get(type);
  }

  public static MetaClass newUncachedInstance(TypeLiteral type) {
    return new JavaReflectionClass(type);
  }

  @Override
  public String getName() {
    return getEnclosedMetaObject().getSimpleName();
  }

  @Override
  public String getFullyQualifiedName() {
    return getEnclosedMetaObject().getName();
  }

  @Override
  public String getCanonicalName() {
    return getEnclosedMetaObject().getCanonicalName();
  }


  @Override
  public String getPackageName() {
    return getEnclosedMetaObject().getPackage().getName();
  }

  private static MetaMethod[] fromMethodArray(Method[] methods) {
    List<MetaMethod> methodList = new ArrayList<MetaMethod>();

    for (Method m : methods) {
      methodList.add(new JavaReflectionMethod(m));
    }

    return methodList.toArray(new MetaMethod[methodList.size()]);
  }

  @Override
  public MetaMethod[] getMethods() {
    return fromMethodArray(getEnclosedMetaObject().getMethods());
  }

  @Override
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

  @Override
  public MetaField[] getFields() {
    return fromFieldArray(getEnclosedMetaObject().getFields());
  }

  @Override
  public MetaField[] getDeclaredFields() {
    return fromFieldArray(getEnclosedMetaObject().getDeclaredFields());
  }

  @Override
  public MetaField getField(String name) {
    try {
      return new JavaReflectionField(getEnclosedMetaObject().getDeclaredField(name));
    }
    catch (Exception e) {
      if (getEnclosedMetaObject().isArray() && "length".equals(name)) {
         return new MetaField.ArrayLengthMetaField(this);
      }
      
      throw new RuntimeException("Could not get field: " + name, e);
    }
  }

  @Override
  public MetaField getDeclaredField(String name) {
    try {
      return new JavaReflectionField(getEnclosedMetaObject().getDeclaredField(name));
    }
    catch (Exception e) {
      throw new RuntimeException("Could not get field: " + name, e);
    }
  }

  @Override
  public MetaConstructor[] getConstructors() {
    List<MetaConstructor> constructorList = new ArrayList<MetaConstructor>();

    for (Constructor c : getEnclosedMetaObject().getConstructors()) {
      constructorList.add(new JavaReflectionConstructor(c));
    }

    return constructorList.toArray(new MetaConstructor[constructorList.size()]);
  }

  @Override
  public MetaConstructor[] getDeclaredConstructors() {
    List<MetaConstructor> constructorList = new ArrayList<MetaConstructor>();

    for (Constructor c : getEnclosedMetaObject().getDeclaredConstructors()) {
      constructorList.add(new JavaReflectionConstructor(c));
    }

    return constructorList.toArray(new MetaConstructor[constructorList.size()]);
  }

  @Override
  public MetaConstructor getConstructor(Class... parameters) {
    try {
      return new JavaReflectionConstructor(getEnclosedMetaObject().getConstructor(parameters));
    }
    catch (Exception e) {
      throw new RuntimeException("Could not get constructor", e);
    }
  }

  @Override
  public MetaConstructor getDeclaredConstructor(Class... parameters) {
    try {
      return new JavaReflectionConstructor(getEnclosedMetaObject().getDeclaredConstructor(parameters));
    }
    catch (Exception e) {
      throw new RuntimeException("Could not get constructor", e);
    }
  }

  @Override
  public MetaClass[] getInterfaces() {
    List<MetaClass> metaClassList = new ArrayList<MetaClass>();
    Type[] genIface = getEnclosedMetaObject().getGenericInterfaces();

    int i = 0;
    for (Class<?> type : getEnclosedMetaObject().getInterfaces()) {
      if (genIface != null) {
        metaClassList.add(new JavaReflectionClass(type, genIface[i]));
      }
      else {
        metaClassList.add(new JavaReflectionClass(type));
      }
      i++;
    }

    return metaClassList.toArray(new MetaClass[metaClassList.size()]);
  }

  @Override
  public MetaClass getSuperClass() {
    return MetaClassFactory.get(getEnclosedMetaObject().getSuperclass());
  }

  @Override
  public MetaClass getComponentType() {
    return MetaClassFactory.get(getEnclosedMetaObject().getComponentType());
  }

  @Override
  public Annotation[] getAnnotations() {
    if (annotationsCache == null) {
      annotationsCache = getEnclosedMetaObject().getAnnotations();
    }
    return annotationsCache;
  }

  @Override
  public MetaTypeVariable[] getTypeParameters() {
    return JavaReflectionUtil.fromTypeVariable(getEnclosedMetaObject().getTypeParameters());
  }


  @Override
  public boolean isPrimitive() {
    return getEnclosedMetaObject().isPrimitive();
  }

  @Override
  public boolean isVoid() {
    return getEnclosedMetaObject().equals(Void.TYPE);
  }

  @Override
  public boolean isInterface() {
    return getEnclosedMetaObject().isInterface();
  }

  @Override
  public boolean isAbstract() {
    return (getEnclosedMetaObject().getModifiers() & Modifier.ABSTRACT) != 0;
  }

  @Override
  public boolean isArray() {
    return getEnclosedMetaObject().isArray();
  }

  @Override
  public boolean isEnum() {
    return getEnclosedMetaObject().isEnum();
  }

  @Override
  public boolean isAnnotation() {
    return getEnclosedMetaObject().isAnnotation();
  }

  @Override
  public boolean isPublic() {
    return (getEnclosedMetaObject().getModifiers() & Modifier.PUBLIC) != 0;
  }

  @Override
  public boolean isPrivate() {
    return (getEnclosedMetaObject().getModifiers() & Modifier.PRIVATE) != 0;
  }

  @Override
  public boolean isProtected() {
    return (getEnclosedMetaObject().getModifiers() & Modifier.PROTECTED) != 0;
  }

  @Override
  public boolean isFinal() {
    return (getEnclosedMetaObject().getModifiers() & Modifier.FINAL) != 0;
  }

  @Override
  public boolean isStatic() {
    return (getEnclosedMetaObject().getModifiers() & Modifier.STATIC) != 0;
  }

  @Override
  public MetaClass asArrayOf(int dimensions) {
    return MetaClassFactory.getArrayOf(getEnclosedMetaObject(), dimensions);
  }

  @Override
  public String toString() {
    return getFullyQualifiedName();
  }

}
