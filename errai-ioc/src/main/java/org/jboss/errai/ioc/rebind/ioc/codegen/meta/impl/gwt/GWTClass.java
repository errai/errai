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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaConstructor;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaField;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaTypeVariable;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.AbstractMetaClass;

import com.google.gwt.core.ext.typeinfo.JArrayType;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JConstructor;
import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JGenericType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameterizedType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.JTypeParameter;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class GWTClass extends AbstractMetaClass<JType> {
  private Annotation[] annotationsCache;

  private GWTClass(JType classType) {
    super(classType);
    JParameterizedType parameterizedType = classType.isParameterized();
    if (parameterizedType != null) {
      super.parameterizedType = new GWTParameterizedType(parameterizedType);
    }
  }

  public static MetaClass newInstance(JType type) {
    return MetaClassFactory.get(type);
  }

  public static MetaClass newUncachedInstance(JType type) {
    return new GWTClass(type);
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
    return getEnclosedMetaObject().getJNISignature().replace("/", ".");
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

      metaClassList.add(new GWTClass(type));
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

    return MetaClassFactory.get(type.getSuperclass());
  }

  @Override
  public MetaClass getComponentType() {
    JArrayType type = getEnclosedMetaObject().isArray();
    if (type == null) {
      return null;
    }
    return MetaClassFactory.get(type.getComponentType());
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
  public String toString() {
    return getFullyQualifiedName();
  }

}
