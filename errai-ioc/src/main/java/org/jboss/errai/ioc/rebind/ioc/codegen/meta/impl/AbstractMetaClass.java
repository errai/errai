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

package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl;

import com.google.gwt.core.ext.typeinfo.JClassType;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.*;
import org.mvel2.util.ParseTools;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.jboss.errai.ioc.rebind.ioc.InjectUtil.classToMeta;
import static org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClassFactory.asClassArray;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public abstract class AbstractMetaClass<T> extends MetaClass {
  private final T enclosedMetaObject;
  protected MetaParameterizedType parameterizedType;

  protected AbstractMetaClass(T enclosedMetaObject) {
    this.enclosedMetaObject = enclosedMetaObject;
  }

  @Override
  public String getFullyQualifiedNameWithTypeParms() {
    StringBuilder buf = new StringBuilder(getFullyQualifiedName());
    buf.append(getTypeParmsString(getParameterizedType()));
    return buf.toString();
  }

  private String getTypeParmsString(MetaParameterizedType parameterizedType) {
    StringBuilder buf = new StringBuilder();

    if (parameterizedType != null && parameterizedType.getTypeParameters().length != 0) {
      buf.append("<");
      for (int i = 0; i < parameterizedType.getTypeParameters().length; i++) {

        MetaType typeParameter = parameterizedType.getTypeParameters()[i];
        if (typeParameter instanceof MetaParameterizedType) {
          MetaParameterizedType parameterizedTypeParemeter = (MetaParameterizedType) typeParameter;
          buf.append(((MetaClass) parameterizedTypeParemeter.getRawType()).getFullyQualifiedName());
          buf.append(getTypeParmsString(parameterizedTypeParemeter));
        }
        else {
          buf.append(((MetaClass) typeParameter).getFullyQualifiedName());
        }

        if (i + 1 < parameterizedType.getTypeParameters().length)
          buf.append(", ");
      }

      buf.append(">");
    }
    return buf.toString();
  }

  protected static MetaMethod _getMethod(MetaMethod[] methods, String name, MetaClass... parmTypes) {
    Outer:
    for (MetaMethod method : methods) {
      if (method.getName().equals(name) && method.getParameters().length == parmTypes.length) {
        for (int i = 0; i < parmTypes.length; i++) {
          if (!method.getParameters()[i].getType().equals(parmTypes[i])) {
            continue Outer;
          }
        }
        return method;
      }
    }
    return null;
  }

  protected static MetaConstructor _getConstructor(MetaConstructor[] constructors, MetaClass... parmTypes) {
    Outer:
    for (MetaConstructor constructor : constructors) {
      if (constructor.getParameters().length == parmTypes.length) {
        for (int i = 0; i < parmTypes.length; i++) {
          if (!constructor.getParameters()[i].getType().equals(parmTypes[i])) {
            continue Outer;
          }
        }
        return constructor;
      }
    }
    return null;
  }

  @Override
  public MetaMethod getMethod(String name, Class... parmTypes) {
    return _getMethod(getMethods(), name, classToMeta(parmTypes));
  }

  @Override
  public MetaMethod getMethod(String name, MetaClass... parameters) {
    return _getMethod(getMethods(), name, parameters);
  }

  @Override
  public MetaMethod getDeclaredMethod(String name, Class... parmTypes) {
    return _getMethod(getDeclaredMethods(), name, classToMeta(parmTypes));
  }

  @Override
  public MetaMethod getDeclaredMethod(String name, MetaClass... parmTypes) {
    return _getMethod(getDeclaredMethods(), name, parmTypes);
  }

  @Override
  public MetaMethod getBestMatchingMethod(String name, Class... parameters) {
    Class<?> cls = asClass();
    if (cls != null) {
      Method m = ParseTools.getBestCandidate(parameters, name, cls, cls.getMethods(), false);
      if (m == null)
        return null;
      return getMethod(name, m.getParameterTypes());
    }
    else {
      return getMethod(name, parameters);
    }
  }

  @Override
  public MetaMethod getBestMatchingMethod(String name, MetaClass... parameters) {
    return getBestMatchingMethod(name, asClassArray(parameters));
  }

  @Override
  public MetaMethod getBestMatchingStaticMethod(String name, Class... parameters) {
    Class<?> cls = asClass();

    Method m = ParseTools.getBestCandidate(parameters, name, cls,
            fromMetaMethod(getStaticMethods()), false);
    if (m == null)
      return null;

    return getMethod(name, m.getParameterTypes());
  }

  @Override
  public MetaMethod getBestMatchingStaticMethod(String name, MetaClass... parameters) {
    return getBestMatchingStaticMethod(name, asClassArray(parameters));
  }

  private MetaMethod[] getStaticMethods() {
    List<MetaMethod> methods = new ArrayList<MetaMethod>();

    for (MetaMethod method : getMethods()) {
      if (method.isStatic()) {
        methods.add(method);
      }
    }

    return methods.toArray(new MetaMethod[methods.size()]);
  }

  private static Method[] fromMetaMethod(MetaMethod[] methods) {
    if (methods == null || methods.length == 0) {
      return new Method[0];
    }

    List<Method> staticMethods = new ArrayList<Method>();

    for (MetaMethod m : methods) {
      staticMethods.add(getJavaMethodFromMetaMethod(m));
    }

    return staticMethods.toArray(new Method[staticMethods.size()]);
  }

  private static Method getJavaMethodFromMetaMethod(MetaMethod method) {
    Class<?> declaring = method.getDeclaringClass().asClass();
    Class<?>[] parms = getParmTypes(method.getParameters());

    try {
      return declaring.getMethod(method.getName(), parms);
    }
    catch (NoSuchMethodException e) {
      return null;
    }
  }

  private static Class<?>[] getParmTypes(MetaParameter[] parameters) {
    List<Class<?>> parmTypes = new ArrayList<Class<?>>();

    for (MetaParameter parameter : parameters) {
      parmTypes.add(parameter.getType().asClass());
    }

    return parmTypes.toArray(new Class<?>[parmTypes.size()]);
  }

  @Override
  public MetaConstructor getBestMatchingConstructor(Class... parameters) {
    Class<?> cls = asClass();
    Constructor c = ParseTools.getBestConstructorCandidate(parameters, cls, false);
    if (c == null)
      return null;

    MetaClass metaClass = MetaClassFactory.get(cls);
    return metaClass.getConstructor(c.getParameterTypes());
  }

  @Override
  public MetaConstructor getBestMatchingConstructor(MetaClass... parameters) {
    return getBestMatchingConstructor(asClassArray(parameters));
  }

  @Override
  public MetaConstructor getConstructor(Class... parameters) {
    return _getConstructor(getConstructors(), classToMeta(parameters));
  }

  @Override
  public MetaConstructor getConstructor(MetaClass... parameters) {
    return _getConstructor(getConstructors(), parameters);
  }

  @Override
  public MetaConstructor getDeclaredConstructor(Class... parameters) {
    return _getConstructor(getDeclaredConstructors(), classToMeta(parameters));
  }

  @Override
  public final <A extends Annotation> A getAnnotation(Class<A> annotation) {
    for (Annotation a : getAnnotations()) {
      if (a.annotationType().equals(annotation))
        return (A) a;
    }
    return null;
  }

  @Override
  public final boolean isAnnotationPresent(Class<? extends Annotation> annotation) {
    return getAnnotation(annotation) != null;
  }

  public T getEnclosedMetaObject() {
    return enclosedMetaObject;
  }

  private String hashString;

  private String hashString() {
    if (hashString == null) {
      hashString = MetaClass.class.getName() + ":" + getFullyQualifiedName();
    }
    return hashString;
  }

  @Override
  public boolean isAssignableFrom(MetaClass clazz) {
    MetaClass cls = clazz;
    do {
      if (this.equals(cls))
        return true;
    }
    while ((cls = cls.getSuperClass()) != null);

    return _hasInterface(clazz.getInterfaces(), this);
  }

  @Override
  public boolean isAssignableTo(MetaClass clazz) {
    MetaClass cls = this;
    do {
      if (cls.equals(clazz))
        return true;
    }
    while ((cls = cls.getSuperClass()) != null);

    return _hasInterface(getInterfaces(), clazz);
  }

  private static boolean _hasInterface(MetaClass[] from, MetaClass to) {
    for (MetaClass iface : from) {
      if (to.equals(iface))
        return true;
      else if (_hasInterface(iface.getInterfaces(), to))
        return true;
    }

    return false;
  }

  @Override
  public boolean isAssignableFrom(Class clazz) {
    return isAssignableFrom(MetaClassFactory.get(clazz));
  }

  @Override
  public boolean isAssignableTo(Class clazz) {
    return isAssignableTo(MetaClassFactory.get(clazz));
  }

  @Override
  public boolean isAssignableTo(JClassType clazz) {
    return isAssignableFrom(MetaClassFactory.get(clazz));
  }

  @Override
  public boolean isAssignableFrom(JClassType clazz) {
    return isAssignableTo(MetaClassFactory.get(clazz));
  }

  @Override
  public MetaParameterizedType getParameterizedType() {
    return parameterizedType;
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof MetaClass && hashString().equals(MetaClass.class.getName()
            + ":" + ((MetaClass) o).getFullyQualifiedName());
  }

  @Override
  public int hashCode() {
    return hashString().hashCode();
  }

  @Override
  public Class<?> asClass() {
    Class<?> cls = null;

    if (enclosedMetaObject instanceof Class) {
      cls = (Class<?>) enclosedMetaObject;
    }
    else if (enclosedMetaObject != null) {
      try {
        cls = Class.forName(((JClassType) enclosedMetaObject).getQualifiedSourceName(), false,
                Thread.currentThread().getContextClassLoader());
      }
      catch (ClassNotFoundException e) {
        //
      }
    }

    if (cls != null && cls.isArray() && MetaType.class.isAssignableFrom(cls.getComponentType())) {
      try {

        Class<?> type = cls.getClass();
        int dim = 1;
        while (type.isArray()) {
          dim++;
          type = type.getComponentType();
        }

        String dimString = "";
        for (int i = 0; i < dim; i++) {
          dimString += "[";
        }

        cls = Class.forName(dimString + "L" + type.getName() + ";", false,
                Thread.currentThread().getContextClassLoader());
      }
      catch (ClassNotFoundException e) {
        e.printStackTrace();
        cls = null;
      }
    }

    return cls;
  }

  @Override
  public MetaClass asBoxed() {
    return MetaClassFactory.get(ParseTools.boxPrimitive(asClass()));
  }

  @Override
  public MetaClass asUnboxed() {
    return MetaClassFactory.get(ParseTools.unboxPrimitive(asClass()));
  }
}
