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

package org.jboss.errai.codegen.meta.impl.gwt;

import com.google.gwt.core.ext.typeinfo.JArrayType;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JConstructor;
import com.google.gwt.core.ext.typeinfo.JEnumType;
import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JGenericType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameter;
import com.google.gwt.core.ext.typeinfo.JParameterizedType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.JTypeParameter;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import org.jboss.errai.codegen.DefModifiers;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.builder.impl.Scope;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaConstructor;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaTypeVariable;
import org.jboss.errai.codegen.meta.impl.AbstractMetaClass;
import org.jboss.errai.codegen.util.GenUtil;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class GWTClass extends AbstractMetaClass<JType> {
  protected Annotation[] annotationsCache;
  protected TypeOracle oracle;

  static {
    GenUtil.addClassAlias(GWTClass.class);
  }

  protected GWTClass(TypeOracle oracle, JType classType, boolean erased) {
    super(classType);
    this.oracle = oracle;

    JParameterizedType parameterizedType = classType.isParameterized();
    if (!erased) {
      if (parameterizedType != null) {
        super.parameterizedType = new GWTParameterizedType(oracle, parameterizedType);
      }
    }
  }

  public static MetaClass newInstance(TypeOracle oracle, JType type) {
    return newUncachedInstance(oracle, type);
  }

  public static MetaClass newInstance(TypeOracle oracle, String type) {
    try {
      return newUncachedInstance(oracle, oracle.getType(type));
    }
    catch (NotFoundException e) {
      return null;
    }
  }

  public static MetaClass newUncachedInstance(TypeOracle oracle, JType type) {


    return new GWTClass(oracle, type, false);
  }

  public static MetaClass newUncachedInstance(TypeOracle oracle, JType type, boolean erased) {
    return new GWTClass(oracle, type, erased);
  }


  public static MetaClass[] fromClassArray(TypeOracle oracle, JClassType[] classes) {
    MetaClass[] newClasses = new MetaClass[classes.length];
    for (int i = 0; i < classes.length; i++) {
      newClasses[i] = newInstance(oracle, classes[i]);
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
    if (isArray()) {
      if (getOuterComponentType().isPrimitive()) {
        return getInternalName();
      }
      else {
        return getInternalName().replaceAll("/", "\\.");
      }
    }
    else {
      return getEnclosedMetaObject().getQualifiedBinaryName();
    }
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

  private static MetaMethod[] fromMethodArray(TypeOracle oracle, JMethod[] methods) {
    List<MetaMethod> methodList = new ArrayList<MetaMethod>();

    for (JMethod m : methods) {
      methodList.add(new GWTMethod(oracle, m));
    }

    return methodList.toArray(new MetaMethod[methodList.size()]);
  }

  private List<MetaMethod> getSpecialTypeMethods() {
    List<MetaMethod> meths = new ArrayList<MetaMethod>();
    JEnumType type = getEnclosedMetaObject().isEnum();

    if (type != null) {

      meths.add(new GWTSpecialMethod(this, DefModifiers.none(), Scope.Public, String.class, "name"));
      meths.add(new GWTSpecialMethod(this, DefModifiers.none(), Scope.Public, Enum.class, "valueOf", Parameter.of(String.class, "p").getMetaParameter()));
      meths.add(new GWTSpecialMethod(this, DefModifiers.none(), Scope.Public, Enum[].class, "values"));

    }

    return meths;
  }

  @Override
  public MetaMethod[] getMethods() {
    Set<MetaMethod> meths = new LinkedHashSet<MetaMethod>();
    meths.addAll(getSpecialTypeMethods());

    JClassType type = getEnclosedMetaObject().isClassOrInterface();
    if (type == null) {
      return null;
    }

    do {
      for (JMethod jMethod : type.getMethods()) {
        if (!jMethod.isPrivate()) {
          meths.add(new GWTMethod(oracle, jMethod));
        }
      }

      for (JClassType iface : type.getImplementedInterfaces()) {
        meths.addAll(Arrays.asList(GWTClass.newInstance(oracle, iface).getMethods()));
      }
    }
    while ((type = type.getSuperclass()) != null);

    return meths.toArray(new MetaMethod[meths.size()]);
  }

  @Override
  public MetaMethod[] getDeclaredMethods() {
    JClassType type = getEnclosedMetaObject().isClassOrInterface();
    if (type == null) {
      return null;
    }

    return fromMethodArray(oracle, type.getMethods());
  }

  private static MetaField[] fromFieldArray(TypeOracle oracle, JField[] methods) {
    List<MetaField> methodList = new ArrayList<MetaField>();

    for (JField f : methods) {
      methodList.add(new GWTField(oracle, f));
    }

    return methodList.toArray(new MetaField[methodList.size()]);
  }

  @Override
  public MetaField[] getFields() {
    JClassType type = getEnclosedMetaObject().isClassOrInterface();
    if (type == null) {
      return null;
    }
    return fromFieldArray(oracle, type.getFields());
  }

  @Override
  public MetaField[] getDeclaredFields() {
    return getFields();
  }

  @Override
  public MetaField getField(String name) {
    JClassType type = getEnclosedMetaObject().isClassOrInterface();
    if (type == null) {
      if ("length".equals(name) && getEnclosedMetaObject().isArray() != null) {
        return new MetaField.ArrayLengthMetaField(this);
      }
      return null;
    }


    JField field = type.getField(name);

    if (field == null) {
      throw new RuntimeException("no such field: " + name + " in class: " + this);
    }

    return new GWTField(oracle, field);
  }

  @Override
  public MetaField getDeclaredField(String name) {
    return getField(name);
  }

  private static MetaConstructor[] fromMethodArray(TypeOracle oracle, JConstructor[] constructors) {
    List<MetaConstructor> constructorList = new ArrayList<MetaConstructor>();

    for (JConstructor c : constructors) {
      constructorList.add(new GWTConstructor(oracle, c));
    }

    return constructorList.toArray(new MetaConstructor[constructorList.size()]);
  }

  @Override
  public MetaConstructor[] getConstructors() {
    JClassType type = getEnclosedMetaObject().isClassOrInterface();
    if (type == null) {
      return null;
    }

    return fromMethodArray(oracle, type.getConstructors());
  }

  @Override
  public MetaConstructor[] getDeclaredConstructors() {
    return getConstructors();
  }

  @Override
  public MetaClass[] getInterfaces() {
    JClassType jClassType = getEnclosedMetaObject().isClassOrInterface();
    if (jClassType == null) return new MetaClass[0];

    List<MetaClass> metaClassList = new ArrayList<MetaClass>();
    for (JClassType type : jClassType.getImplementedInterfaces()) {

      metaClassList.add(new GWTClass(oracle, type, false));
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

    type = type.getSuperclass();

    if (type == null) {
      return null;
    }

    return newUncachedInstance(oracle, type);
  }

  @Override
  public MetaClass getComponentType() {
    JArrayType type = getEnclosedMetaObject().isArray();
    if (type == null) {
      return null;
    }
    return newUncachedInstance(oracle, type.getComponentType());
  }

  @Override
  public Annotation[] getAnnotations() {
    if (annotationsCache == null) {
      JClassType classOrInterface = getEnclosedMetaObject().isClassOrInterface();

      if (classOrInterface != null) {
        annotationsCache = classOrInterface.getAnnotations();
      }

      if (annotationsCache == null) {
        annotationsCache = new Annotation[0];
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
        typeVariables.add(new GWTTypeVariable(oracle, typeParameter));
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
  public boolean isSynthetic() {
    return false;
  }

  @Override
  public boolean isAnonymousClass() {
    return false;
  }

  @Override
  public MetaClass asArrayOf(int dimensions) {
    JType type = getEnclosedMetaObject();
    for (int i = 0; i < dimensions; i++) {
      type = oracle.getArrayType(type);
    }

    return new GWTClass(oracle, type, false);
  }
}
