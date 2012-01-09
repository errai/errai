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

package org.jboss.errai.codegen.framework.meta.impl;

import static org.jboss.errai.codegen.framework.meta.MetaClassFactory.asClassArray;
import static org.jboss.errai.codegen.framework.util.GenUtil.classToMeta;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.meta.MetaConstructor;
import org.jboss.errai.codegen.framework.meta.MetaMethod;
import org.jboss.errai.codegen.framework.meta.MetaParameter;
import org.jboss.errai.codegen.framework.meta.MetaParameterizedType;
import org.jboss.errai.codegen.framework.meta.MetaType;
import org.mvel2.util.ParseTools;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public abstract class AbstractMetaClass<T> extends MetaClass {
  private final T enclosedMetaObject;
  protected MetaParameterizedType parameterizedType;
  protected MetaParameterizedType genericSuperClass;

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
    MetaMethod candidate = null;
    int bestScore = 0;
    int score;

    for (MetaMethod method : methods) {
      score = 0;
      if (method.getName().equals(name)) {
        if (method.getParameters().length == parmTypes.length) {
          if (parmTypes.length == 0) {
            score = 1;
            MetaClass retType = method.getReturnType();
            while ((retType = retType.getSuperClass()) != null) score++;
          }
          else {
            for (int i = 0; i < parmTypes.length; i++) {
              if (method.getParameters()[i].getType().isAssignableFrom(parmTypes[i])) {
                score++;
                if (method.getParameters()[i].getType().equals(parmTypes[i])) {
                  score++;
                }
              }
            }
          }
        }
      }

      if (score > bestScore) {
        bestScore = score;
        candidate = method;
      }
    }

    return candidate;
  }

  protected static MetaConstructor _getConstructor(MetaConstructor[] constructors, MetaClass... parmTypes) {
    MetaConstructor candidate = null;
    int bestScore = 0;
    int score;

    for (MetaConstructor constructor : constructors) {
      score = 0;
      if (constructor.getParameters().length == parmTypes.length) {
        if (parmTypes.length == 0) {
          score = 1;
        }
        else {
          for (int i = 0; i < parmTypes.length; i++) {
            if (constructor.getParameters()[i].getType().isAssignableFrom(parmTypes[i])) {
              score++;
              if (constructor.getParameters()[i].getType().equals(parmTypes[i])) {
                score++;
              }
            }
          }
        }
      }

      if (score > bestScore) {
        bestScore = score;
        candidate = constructor;
      }
    }

    return candidate;
  }

  private Map<String, Map<String, MetaMethod>> METHOD_MATCH_CACHE = new HashMap<String, Map<String, MetaMethod>>();

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
    return getBestMatchingMethod(null, name, parameters);
  }

  @Override
  public MetaMethod getBestMatchingMethod(String name, MetaClass... parameters) {
    return getBestMatchingMethod(name, asClassArray(parameters));
  }

  @Override
  public MetaMethod getBestMatchingStaticMethod(String name, Class... parameters) {
    return getBestMatchingMethod(new GetMethodsCallback() {
      @Override
      public Method[] getMethods() {
        return fromMetaMethod(getStaticMethods());
      }
    }, name, parameters);
  }

  @Override
  public MetaMethod getBestMatchingStaticMethod(String name, MetaClass... parameters) {
    return getBestMatchingStaticMethod(name, asClassArray(parameters));
  }

  private static interface GetMethodsCallback {
    Method[] getMethods();
  }

  private MetaMethod getBestMatchingMethod(GetMethodsCallback methodsCallback, String name, Class... parameters) {
    Map<String, MetaMethod> subMap;
    MetaMethod meth;
    if ((subMap = METHOD_MATCH_CACHE.get(name)) == null) {
      METHOD_MATCH_CACHE.put(name, subMap = new HashMap<String, MetaMethod>());
    }

    String parmKey = Arrays.toString(parameters);

    if ((meth = subMap.get(parmKey)) == null) {
      Class<?> cls = asClass();

      if (cls != null) {
        List<Method> methodList = new ArrayList<Method>();
        for (Method m : (methodsCallback == null) ? cls.getMethods() : methodsCallback.getMethods()) {
          if (!m.isBridge()) {
            methodList.add(m); 
          }
        }

        Method[] methods = methodList.toArray(new Method[methodList.size()]);
        Method m = ParseTools.getBestCandidate(parameters, name, cls, methods, true);
        if (m == null) {
          if (isInterface()) {
            m = ParseTools.getBestCandidate(parameters, name, Object.class, Object.class.getMethods(), true);
          }

          if (m == null) {
            m = ParseTools.getBestCandidate(parameters, name, cls, methods, false);
            if (m == null) {
              if (isInterface()) {
                m = ParseTools.getBestCandidate(parameters, name, Object.class, Object.class.getMethods(), false);
              }

              if (m == null) {
                return null;
              }
            }
          }
        }
//      m = ParseTools.getWidenedTarget(m);
        meth = getMethod(name, m.getParameterTypes());
      }
      else {
        meth = getMethod(name, parameters);
      }

      subMap.put(parmKey, meth);
    }


    return meth;
  }

  private MetaMethod[] staticMethodCache;

  private MetaMethod[] getStaticMethods() {
    if (staticMethodCache != null) {
      return staticMethodCache;
    }

    List<MetaMethod> methods = new ArrayList<MetaMethod>();

    for (MetaMethod method : getMethods()) {
      if (method.isStatic()) {
        methods.add(method);
      }
    }

    return staticMethodCache = methods.toArray(new MetaMethod[methods.size()]);

  }

  private static final Map<MetaMethod[], Method[]> METAMETHOD_TO_METHOD_CACHE = new HashMap<MetaMethod[], Method[]>();

  private static Method[] fromMetaMethod(MetaMethod[] methods) {
    Method[] result = METAMETHOD_TO_METHOD_CACHE.get(methods);
    if (result == null) {

      if (methods == null || methods.length == 0) {
        return new Method[0];
      }

      List<Method> staticMethods = new ArrayList<Method>();

      for (MetaMethod m : methods) {
        Method javaMethod = getJavaMethodFromMetaMethod(m);
        if (javaMethod != null)
          staticMethods.add(javaMethod);
      }

      result = staticMethods.toArray(new Method[staticMethods.size()]);
      METAMETHOD_TO_METHOD_CACHE.put(methods, result);
    }
    return result;
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
    if (cls != null) {
      Constructor c = ParseTools.getBestConstructorCandidate(parameters, cls, true);
      if (c == null) {
        c = ParseTools.getBestConstructorCandidate(parameters, cls, false);
        if (c == null) {
          return null;
        }
      }
      MetaClass metaClass = MetaClassFactory.get(cls);
      return metaClass.getConstructor(c.getParameterTypes());
    }
    else {
      return getConstructor(parameters);
    }
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

  private String _hashString;

  private String hashString() {
    if (_hashString == null) {
      _hashString = MetaClass.class.getName() + ":" + getFullyQualifiedName();
      if (getParameterizedType() != null) {
        _hashString += getParameterizedType().toString();
      }
    }
    return _hashString;
  }

  private Set<MetaClass> POS_ASSIGNABLE_CACHE = new HashSet<MetaClass>();
  private Set<MetaClass> NEG_ASSIGNABLE_CACHE = new HashSet<MetaClass>();

  @Override
  public boolean isAssignableFrom(MetaClass clazz) {
    if (POS_ASSIGNABLE_CACHE.contains(clazz)) return true;
    if (NEG_ASSIGNABLE_CACHE.contains(clazz)) return false;

    MetaClass cls;

    if (equals(cls = MetaClassFactory.get(Object.class))) {
      POS_ASSIGNABLE_CACHE.add(cls);
      return true;
    }

    cls = clazz;

    do {
      if (this.getFullyQualifiedName().equals(cls.getFullyQualifiedName())) {
        POS_ASSIGNABLE_CACHE.add(cls);
        return true;
      }
    }
    while ((cls = cls.getSuperClass()) != null);

    if (_hasInterface(clazz.getInterfaces(), this.getErased())) {
      POS_ASSIGNABLE_CACHE.add(clazz);
      return true;
    }

    NEG_ASSIGNABLE_CACHE.add(clazz);
    return false;
  }

  @Override
  public boolean isAssignableTo(MetaClass clazz) {
    if (clazz.equals(MetaClassFactory.get(Object.class)))
      return true;

    MetaClass cls = this;
    do {
      if (cls.equals(clazz))
        return true;
    }
    while ((cls = cls.getSuperClass()) != null);

    return _hasInterface(getInterfaces(), clazz.getErased());
  }

  private static boolean _hasInterface(MetaClass[] from, MetaClass to) {
    for (MetaClass iface : from) {
      if (to.getFullyQualifiedName().equals(iface.getErased().getFullyQualifiedName()))
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
  public MetaParameterizedType getParameterizedType() {
    return parameterizedType;
  }

  public MetaParameterizedType getGenericSuperClass() {
    return genericSuperClass;
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof AbstractMetaClass && hashString().equals(((AbstractMetaClass) o).hashString());
  }


  int _hashCode;

  @Override
  public int hashCode() {
    if (_hashCode != 0) return _hashCode;
    return _hashCode = hashString().hashCode();
  }

  private volatile transient Class<?> _asClassCache;

  @Override
  public Class<?> asClass() {
    if (_asClassCache != null) {
      return _asClassCache;
    }

    Class<?> cls = null;

    if (enclosedMetaObject instanceof Class) {
      cls = (Class<?>) enclosedMetaObject;
    }
    else if (isArray()) {
      try {
        String name = getInternalName().replaceAll("/", "\\.");

        cls = Class.forName(name, false,
                Thread.currentThread().getContextClassLoader());
      }
      catch (ClassNotFoundException e) {
        e.printStackTrace();
        cls = null;
      }
    }
//    else if (enclosedMetaObject != null) {
//      try {
//        cls = Class.forName(((JClassType) enclosedMetaObject).getQualifiedSourceName(), false,
//                Thread.currentThread().getContextClassLoader());
//      }
//      catch (ClassNotFoundException e) {
//
//      }
//    }

    return _asClassCache = cls;
  }

  private MetaClass _boxedCache;

  @Override
  public MetaClass asBoxed() {
    if (_boxedCache != null) return _boxedCache;
    Class<?> cls = asClass();
    if (cls == null)
      return _boxedCache = this;

    return _boxedCache = MetaClassFactory.get(ParseTools.boxPrimitive(cls));
  }

  private MetaClass _unboxedCache;

  @Override
  public MetaClass asUnboxed() {
    if (_unboxedCache != null) return _unboxedCache;

    Class<?> cls = asClass();
    if (cls == null)
      return _unboxedCache = this;

    return _unboxedCache = MetaClassFactory.get(ParseTools.unboxPrimitive(cls));
  }

  private MetaClass _erasedCache;

  @Override
  public MetaClass getErased() {
    try {
      return _erasedCache != null ? _erasedCache : (_erasedCache = MetaClassFactory.get(getFullyQualifiedName(), true));
    }
    catch (Exception e) {
      return this;
    }
  }

  private String _internalNameCache;

  public String getInternalName() {
    if (_internalNameCache != null) return _internalNameCache;

    String name = getFullyQualifiedName();

    String dimString = "";
    MetaClass type = this;
    if (isArray()) {
      type = type.getComponentType();
      int dim = 1;
      while (type.isArray()) {
        dim++;
        type = type.getComponentType();
      }

      for (int i = 0; i < dim; i++) {
        dimString += "[";
      }

      name = type.getFullyQualifiedName();
    }

    if (type.isPrimitive()) {
      name = getInternalPrimitiveNameFrom(name.trim());
    }
    else {
      name = "L" + getInternalPrimitiveNameFrom(name.trim()).replaceAll("\\.", "/") + ";";
    }

    return _internalNameCache = dimString + name;
  }

  private static String getInternalPrimitiveNameFrom(String name) {
    if ("int".equals(name)) {
      return "I";
    }
    else if ("boolean".equals(name)) {
      return "Z";
    }
    else if ("byte".equals(name)) {
      return "B";
    }
    else if ("char".equals(name)) {
      return "C";
    }
    else if ("short".equals(name)) {
      return "S";
    }
    else if ("long".equals(name)) {
      return "J";
    }
    else if ("float".equals(name)) {
      return "F";
    }
    else if ("double".equals(name)) {
      return "D";
    }
    else if ("void".equals(name)) {
      return "V";
    }
    return name;
  }

  private static Class<?> getPrimitiveRefFrom(String name) {
    if ("int".equals(name)) {
      return int.class;
    }
    else if ("boolean".equals(name)) {
      return boolean.class;
    }
    else if ("byte".equals(name)) {
      return byte.class;
    }
    else if ("char".equals(name)) {
      return char.class;
    }
    else if ("short".equals(name)) {
      return short.class;
    }
    else if ("long".equals(name)) {
      return long.class;
    }
    else if ("float".equals(name)) {
      return float.class;
    }
    else if ("double".equals(name)) {
      return double.class;
    }
    else if ("void".equals(name)) {
      return void.class;
    }
    return null;
  }

  private MetaClass _outerComponentCache;

  @Override
  public MetaClass getOuterComponentType() {
    if (_outerComponentCache != null) return _outerComponentCache;

    MetaClass c = this;
    while (c.isArray()) {
      c = c.getComponentType();
    }
    return _outerComponentCache = c;
  }

  @Override
  public String toString() {
    return getCanonicalName();
  }
}
