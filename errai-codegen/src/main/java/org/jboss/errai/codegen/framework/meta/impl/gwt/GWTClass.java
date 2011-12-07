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

package org.jboss.errai.codegen.framework.meta.impl.gwt;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.ext.typeinfo.*;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.meta.MetaConstructor;
import org.jboss.errai.codegen.framework.meta.MetaField;
import org.jboss.errai.codegen.framework.meta.MetaMethod;
import org.jboss.errai.codegen.framework.meta.MetaTypeVariable;
import org.jboss.errai.codegen.framework.meta.impl.AbstractMetaClass;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class GWTClass extends AbstractMetaClass<JType> {
  private Annotation[] annotationsCache;

  private GWTClass(JType classType, boolean erased) {
    super(classType);
    JParameterizedType parameterizedType = classType.isParameterized();
    if (!erased) {
      if (parameterizedType != null) {
        super.parameterizedType = new GWTParameterizedType(parameterizedType);
      }
    }
  }

  public static MetaClass newInstance(JType type) {
    return newUncachedInstance(type);
  }


  public static MetaClass newInstance(TypeOracle oracle, String type) {
    try {
      return newUncachedInstance(oracle.getType(type));
    }
    catch (NotFoundException e) {
      return null;
    }
  }

  public static MetaClass newUncachedInstance(JType type) {
    return new GWTClass(type, false);
  }

  public static MetaClass newUncachedInstance(JType type, boolean erased) {
    return new GWTClass(type, erased);
  }


  public static MetaClass[] fromClassArray(JClassType[] classes) {
    MetaClass[] newClasses = new MetaClass[classes.length];
    for (int i = 0; i < classes.length; i++) {
      newClasses[i] = newInstance(classes[i]);
    }
    return newClasses;
  }

  public static Class<?>[] jParmToClass(JParameter[] parms) throws ClassNotFoundException {
    Class<?>[] classes = new Class<?>[parms.length];
    for (int i = 0; i < parms.length; i++) {
      classes[i] = getPrimitiveOrClass(parms[i]);
    }
    return classes;
  }

  public static Class<?> getPrimitiveOrClass(JParameter parm) throws ClassNotFoundException {
    JType type = parm.getType();
    String name = type.isArray() != null ? type.getJNISignature().replace("/", ".") : type.getQualifiedSourceName();

    if (parm.getType().isPrimitive() != null) {
      char sig = parm.getType().isPrimitive().getJNISignature().charAt(0);

      switch (sig) {
        case 'Z':
          return boolean.class;
        case 'B':
          return byte.class;
        case 'C':
          return char.class;
        case 'D':
          return double.class;
        case 'F':
          return float.class;
        case 'I':
          return int.class;
        case 'J':
          return long.class;
        case 'S':
          return short.class;
        case 'V':
          return void.class;
        default:
          return null;
      }
    }
    else {
      return Class.forName(name, false, Thread.currentThread().getContextClassLoader());
    }
  }


  @Override
  public String getName() {
    return getEnclosedMetaObject().getSimpleSourceName();
  }

  @Override
  public String getFullyQualifiedName() {
    return getEnclosedMetaObject().getQualifiedSourceName();
  }

  @Override
  public String getCanonicalName() {
    return getEnclosedMetaObject().getQualifiedSourceName();
  }

  @Override
  public String getInternalName() {
    return getEnclosedMetaObject().getJNISignature();
  }

  @Override
  public String getPackageName() {
    String className = getEnclosedMetaObject().getQualifiedSourceName();
    int idx = className.lastIndexOf(".");
    if (idx != -1) {
      return className.substring(0, idx);
    }
    return "";
  }

  private static MetaMethod[] fromMethodArray(JMethod[] methods) {
    List<MetaMethod> methodList = new ArrayList<MetaMethod>();

    for (JMethod m : methods) {
      methodList.add(new GWTMethod(m));
    }

    return methodList.toArray(new MetaMethod[methodList.size()]);
  }

  @Override
  public MetaMethod[] getMethods() {
    JClassType type = getEnclosedMetaObject().isClassOrInterface();
    if (type == null) {
      return null;
    }

    return fromMethodArray(type.getMethods());
  }

  @Override
  public MetaMethod[] getDeclaredMethods() {
    return getMethods();
  }

  private static MetaField[] fromFieldArray(JField[] methods) {
    List<MetaField> methodList = new ArrayList<MetaField>();

    for (JField f : methods) {
      methodList.add(new GWTField(f));
    }

    return methodList.toArray(new MetaField[methodList.size()]);
  }

  @Override
  public MetaField[] getFields() {
    JClassType type = getEnclosedMetaObject().isClassOrInterface();
    if (type == null) {
      return null;
    }
    return fromFieldArray(type.getFields());
  }

  @Override
  public MetaField[] getDeclaredFields() {
    return getFields();
  }

  @Override
  public MetaField getField(String name) {
    JClassType type = getEnclosedMetaObject().isClassOrInterface();
    if (type == null) {
      return null;
    }

    JField field = type.getField(name);

    if (field == null) {
      throw new RuntimeException("no such field: " + field);
    }

    return new GWTField(field);
  }

  @Override
  public MetaField getDeclaredField(String name) {
    return getField(name);
  }

  private static MetaConstructor[] fromMethodArray(JConstructor[] constructors) {
    List<MetaConstructor> constructorList = new ArrayList<MetaConstructor>();

    for (JConstructor c : constructors) {
      constructorList.add(new GWTConstructor(c));
    }

    return constructorList.toArray(new MetaConstructor[constructorList.size()]);
  }

  @Override
  public MetaConstructor[] getConstructors() {
    JClassType type = getEnclosedMetaObject().isClassOrInterface();
    if (type == null) {
      return null;
    }

    return fromMethodArray(type.getConstructors());
  }

  @Override
  public MetaConstructor[] getDeclaredConstructors() {
    return getConstructors();
  }

  @Override
  public MetaClass[] getInterfaces() {
    List<MetaClass> metaClassList = new ArrayList<MetaClass>();
    for (JClassType type : getEnclosedMetaObject().isClassOrInterface()
            .getImplementedInterfaces()) {

      metaClassList.add(new GWTClass(type, false));
    }

    return metaClassList.toArray(new MetaClass[metaClassList.size()]);
  }

  @Override
  public boolean isArray() {
    return getEnclosedMetaObject().isArray() != null;
  }

  @Override
  public MetaClass getSuperClass() {
    JClassType type = getEnclosedMetaObject().isClassOrInterface();
    if (type == null) {
      return null;
    }

    return newUncachedInstance(type.getSuperclass());
  }

  @Override
  public MetaClass getComponentType() {
    JArrayType type = getEnclosedMetaObject().isArray();
    if (type == null) {
      return null;
    }
    return newUncachedInstance(type.getComponentType());
  }

  @Override
  public Annotation[] getAnnotations() {
    if (annotationsCache == null) {
      try {
        Class<?> cls = Class.forName(getEnclosedMetaObject().getQualifiedSourceName(), false,
                Thread.currentThread().getContextClassLoader());

        annotationsCache = cls.getAnnotations();

      }
      catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
    }

    return annotationsCache;
  }

  @Override
  public MetaTypeVariable[] getTypeParameters() {
    List<MetaTypeVariable> typeVariables = new ArrayList<MetaTypeVariable>();
    JGenericType genericType = getEnclosedMetaObject().isGenericType();

    if (genericType != null) {
      for (JTypeParameter typeParameter : genericType.getTypeParameters()) {
        typeVariables.add(new GWTTypeVariable(typeParameter));
      }
    }

    return typeVariables.toArray(new MetaTypeVariable[typeVariables.size()]);
  }

  @Override
  public boolean isVoid() {
    return getEnclosedMetaObject().getSimpleSourceName().equals("void");
  }

  @Override
  public boolean isPrimitive() {
    return getEnclosedMetaObject().isPrimitive() != null;
  }

  @Override
  public boolean isInterface() {
    return getEnclosedMetaObject().isInterface() != null;
  }

  @Override
  public boolean isAbstract() {
    return getEnclosedMetaObject().isClass() != null && getEnclosedMetaObject().isClass().isAbstract();
  }


  @Override
  public boolean isEnum() {
    return getEnclosedMetaObject().isEnum() != null;
  }

  @Override
  public boolean isAnnotation() {
    return getEnclosedMetaObject().isAnnotation() != null;
  }

  @Override
  public boolean isPublic() {
    return getEnclosedMetaObject().isClassOrInterface() != null &&
            getEnclosedMetaObject().isClassOrInterface().isPublic();
  }

  @Override
  public boolean isPrivate() {
    return getEnclosedMetaObject().isClassOrInterface() != null &&
            getEnclosedMetaObject().isClassOrInterface().isPrivate();
  }

  @Override
  public boolean isProtected() {
    return getEnclosedMetaObject().isClassOrInterface() != null &&
            getEnclosedMetaObject().isClassOrInterface().isProtected();
  }

  @Override
  public boolean isFinal() {
    return getEnclosedMetaObject().isClassOrInterface() != null &&
            getEnclosedMetaObject().isClassOrInterface().isFinal();
  }

  @Override
  public boolean isStatic() {
    return getEnclosedMetaObject().isClassOrInterface() != null &&
            getEnclosedMetaObject().isClassOrInterface().isStatic();
  }

  @Override
  public MetaClass asArrayOf(int dimensions) {
    throw new UnsupportedOperationException("asArrayOf() is not supported with GWT types");
  }

  @Override
  public String toString() {
    return getFullyQualifiedName();
  }

}
