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

package org.jboss.errai.codegen.meta.impl;

import static org.jboss.errai.codegen.util.GenUtil.classToMeta;
import static org.jboss.errai.codegen.util.GenUtil.getArrayDimensions;

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.errai.codegen.meta.BeanDescriptor;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaConstructor;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.meta.MetaParameterizedType;
import org.jboss.errai.codegen.meta.MetaType;
import org.jboss.errai.codegen.util.GenUtil;
import org.mvel2.util.NullType;
import org.mvel2.util.ReflectionUtil;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public abstract class AbstractMetaClass<T> extends MetaClass {
  private final T enclosedMetaObject;
  protected MetaParameterizedType parameterizedType;
  protected MetaParameterizedType genericSuperClass;

  protected AbstractMetaClass(final T enclosedMetaObject) {
    this.enclosedMetaObject = enclosedMetaObject;
  }

  @Override
  public String getFullyQualifiedNameWithTypeParms() {
    final StringBuilder buf = new StringBuilder(getFullyQualifiedName());
    buf.append(getTypeParmsString(getParameterizedType()));
    return buf.toString();
  }

  private String getTypeParmsString(final MetaParameterizedType parameterizedType) {
    final StringBuilder buf = new StringBuilder(512);

    if (parameterizedType != null && parameterizedType.getTypeParameters().length != 0) {
      buf.append("<");
      for (int i = 0; i < parameterizedType.getTypeParameters().length; i++) {

        final MetaType typeParameter = parameterizedType.getTypeParameters()[i];
        if (typeParameter instanceof MetaParameterizedType) {
          final MetaParameterizedType parameterizedTypeParameter = (MetaParameterizedType) typeParameter;
          buf.append(((MetaClass) parameterizedTypeParameter.getRawType()).getFullyQualifiedName());
          buf.append(getTypeParmsString(parameterizedTypeParameter));
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

  protected static MetaMethod _getMethod(final MetaMethod[] methods, final String name, final MetaClass... parmTypes) {
    MetaMethod candidate = null;
    int bestScore = 0;
    int score;

    for (final MetaMethod method : methods) {
      score = 0;
      if (method.getName().equals(name)) {
        if (method.getParameters().length == parmTypes.length) {
          if (parmTypes.length == 0) {
            score = 1;
            MetaClass retType = method.getReturnType();
            while ((retType = retType.getSuperClass()) != null)
              score++;
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

  protected static MetaConstructor _getConstructor(final MetaConstructor[] constructors, final MetaClass... parmTypes) {
    MetaConstructor candidate = null;
    int bestScore = 0;
    int score;

    for (final MetaConstructor constructor : constructors) {
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

  @Override
  public MetaMethod getMethod(final String name, final Class... parmTypes) {
    return _getMethod(getMethods(), name, classToMeta(parmTypes));
  }

  @Override
  public MetaMethod getMethod(final String name, final MetaClass... parameters) {
    return _getMethod(getMethods(), name, parameters);
  }

  @Override
  public MetaMethod getDeclaredMethod(final String name, final Class... parmTypes) {
    return _getMethod(getDeclaredMethods(), name, classToMeta(parmTypes));
  }

  @Override
  public MetaMethod getDeclaredMethod(final String name, final MetaClass... parmTypes) {
    return _getMethod(getDeclaredMethods(), name, parmTypes);
  }

  @Override
  public MetaMethod getBestMatchingMethod(final String name, final Class... parameters) {
    MetaMethod meth = getMethod(name, parameters);
    if (meth == null || meth.isStatic()) {
      meth = null;
    }

    final MetaClass[] mcParms = new MetaClass[parameters.length];
    for (int i = 0; i < parameters.length; i++) {
      mcParms[i] = MetaClassFactory.get(parameters[i]);
    }

    if (meth == null) {
      meth = getBestMatchingMethod(new GetMethodsCallback() {
        @Override
        public MetaMethod[] getMethods() {
          return AbstractMetaClass.this.getMethods();
        }
      }, name, mcParms);
    }

    return meth;
  }

  @Override
  public MetaMethod getBestMatchingMethod(final String name, final MetaClass... parameters) {
    return getBestMatchingMethod(new GetMethodsCallback() {
      @Override
      public MetaMethod[] getMethods() {
        return AbstractMetaClass.this.getMethods();
      }
    }, name, parameters);
  }

  @Override
  public MetaMethod getBestMatchingStaticMethod(final String name, final Class... parameters) {
    MetaMethod meth = getMethod(name, parameters);
    if (meth == null || !meth.isStatic()) {
      meth = null;
    }

    final MetaClass[] mcParms = new MetaClass[parameters.length];
    for (int i = 0; i < parameters.length; i++) {
      mcParms[i] = MetaClassFactory.get(parameters[i]);
    }

    if (meth == null) {
      meth = getBestMatchingMethod(new GetMethodsCallback() {
        @Override
        public MetaMethod[] getMethods() {
          return getStaticMethods();
        }
      }, name, mcParms);
    }
    return meth;
  }

  @Override
  public MetaMethod getBestMatchingStaticMethod(final String name, final MetaClass... parameters) {
    return getBestMatchingMethod(new GetMethodsCallback() {
      @Override
      public MetaMethod[] getMethods() {
        return getStaticMethods();
      }
    }, name, parameters);
  }

  private static interface GetMethodsCallback {
    MetaMethod[] getMethods();
  }

  private MetaMethod getBestMatchingMethod(final GetMethodsCallback methodsCallback, final String name,
      final MetaClass... parameters) {
    MetaMethod meth = GenUtil.getBestCandidate(parameters, name, this, methodsCallback.getMethods(), false);
    if (meth == null) {
      meth = GenUtil.getBestCandidate(parameters, name, this, methodsCallback.getMethods(), false);
    }
    return meth;
  }

  private MetaMethod[] staticMethodCache;

  private MetaMethod[] getStaticMethods() {
    if (staticMethodCache != null) {
      return staticMethodCache;
    }

    final List<MetaMethod> methods = new ArrayList<MetaMethod>();

    for (final MetaMethod method : getMethods()) {
      if (method.isStatic()) {
        methods.add(method);
      }
    }

    return staticMethodCache = methods.toArray(new MetaMethod[methods.size()]);
  }

  private static final Map<MetaMethod[], Method[]> METAMETHOD_TO_METHOD_CACHE = new HashMap<MetaMethod[], Method[]>();

  private static Method[] fromMetaMethod(final MetaMethod[] methods) {
    Method[] result = METAMETHOD_TO_METHOD_CACHE.get(methods);
    if (result == null) {

      if (methods == null || methods.length == 0) {
        return new Method[0];
      }

      final List<Method> staticMethods = new ArrayList<Method>();

      for (final MetaMethod m : methods) {
        final Method javaMethod = getJavaMethodFromMetaMethod(m);
        if (javaMethod != null)
          staticMethods.add(javaMethod);
      }

      result = staticMethods.toArray(new Method[staticMethods.size()]);
      METAMETHOD_TO_METHOD_CACHE.put(methods, result);
    }
    return result;
  }

  private static Method getJavaMethodFromMetaMethod(final MetaMethod method) {
    final Class<?> declaring = method.getDeclaringClass().asClass();
    final Class<?>[] parms = getParmTypes(method.getParameters());

    try {
      return declaring.getMethod(method.getName(), parms);
    }
    catch (NoSuchMethodException e) {
      return null;
    }
  }

  private static Class<?>[] getParmTypes(final MetaParameter[] parameters) {
    final List<Class<?>> parmTypes = new ArrayList<Class<?>>();

    for (final MetaParameter parameter : parameters) {
      parmTypes.add(parameter.getType().asClass());
    }

    return parmTypes.toArray(new Class<?>[parmTypes.size()]);
  }

  @Override
  public MetaConstructor getBestMatchingConstructor(final Class... parameters) {
    return getBestMatchingConstructor(MetaClassFactory.fromClassArray(parameters));
  }

  @Override
  public MetaConstructor getBestMatchingConstructor(final MetaClass... parameters) {
    return GenUtil.getBestConstructorCandidate(parameters, this, getConstructors(), false);
  }

  @Override
  public MetaConstructor getConstructor(final Class... parameters) {
    return _getConstructor(getConstructors(), classToMeta(parameters));
  }

  @Override
  public MetaConstructor getConstructor(final MetaClass... parameters) {
    return _getConstructor(getConstructors(), parameters);
  }

  @Override
  public MetaConstructor getDeclaredConstructor(final Class... parameters) {
    return _getConstructor(getDeclaredConstructors(), classToMeta(parameters));
  }

  @Override
  public MetaField getInheritedField(final String name) {
    MetaField f = getDeclaredField(name);
    if (f != null)
      return f;
    for (final MetaClass iface : getInterfaces()) {
      f = iface.getInheritedField(name);
      if (f != null)
        return f;
    }
    if (getSuperClass() != null) {
      return getSuperClass().getInheritedField(name);
    }
    return null;
  }

  @Override
  public final <A extends Annotation> A getAnnotation(final Class<A> annotation) {
    for (final Annotation a : getAnnotations()) {
      if (a.annotationType().equals(annotation))
        return (A) a;
    }
    return null;
  }

  @Override
  public final boolean isAnnotationPresent(final Class<? extends Annotation> annotation) {
    return getAnnotation(annotation) != null;
  }

  // docs inherited from superclass
  @Override
  public final List<MetaMethod> getMethodsAnnotatedWith(final Class<? extends Annotation> annotation) {
    final List<MetaMethod> methods = new ArrayList<MetaMethod>();
    MetaClass scanTarget = this;
    while (scanTarget != null) {
      for (final MetaMethod m : scanTarget.getDeclaredMethods()) {
        if (m.isAnnotationPresent(annotation)) {
          methods.add(m);
        }
      }
      scanTarget = scanTarget.getSuperClass();
    }
    return Collections.unmodifiableList(methods); // in case we want to cache this in the future
  }

  // docs inherited from superclass
  @Override
  public final List<MetaField> getFieldsAnnotatedWith(final Class<? extends Annotation> annotation) {
    final List<MetaField> fields = new ArrayList<MetaField>();
    MetaClass scanTarget = this;
    while (scanTarget != null) {
      for (final MetaField m : scanTarget.getDeclaredFields()) {
        if (m.isAnnotationPresent(annotation)) {
          fields.add(m);
        }
      }
      scanTarget = scanTarget.getSuperClass();
    }
    return Collections.unmodifiableList(fields); // in case we want to cache this in the future
  }

  public T getEnclosedMetaObject() {
    return enclosedMetaObject;
  }

  private String _hashString;

  private String hashString() {
    if (_hashString == null) {
      _hashString = MetaClass.class.getName().concat(":").concat(getFullyQualifiedName());
      if (getParameterizedType() != null) {
        _hashString += getParameterizedType().toString();
      }
    }
    return _hashString;
  }

  private final Map<MetaClass, Boolean> ASSIGNABLE_CACHE = new HashMap<MetaClass, Boolean>();

  private static final MetaClass NULL_TYPE = MetaClassFactory.get(NullType.class);

  @Override
  public boolean isAssignableFrom(final MetaClass clazz) {
    Boolean assignable = ASSIGNABLE_CACHE.get(clazz);
    if (assignable != null) {
      return assignable;
    }

    // XXX not sure if this is uncached on purpose.
    // FIXME there are no tests or documentation for this case
    if (!isPrimitive() && NULL_TYPE.equals(clazz))
      return true;

    if (isArray() && clazz.isArray()) {
      return getOuterComponentType().equals(clazz.getOuterComponentType())
          && getArrayDimensions(this) == getArrayDimensions(clazz);
    }

    final MetaClass sup;

    if (MetaClassFactory.get(Object.class).equals(this)) {
      assignable = true;
    }
    else if (this.getFullyQualifiedName().equals(clazz.getFullyQualifiedName())) {
      assignable = true;
    }
    else if (_hasInterface(clazz.getInterfaces(), this.getErased())) {
      assignable = true;
    }
    else
      assignable = (sup = clazz.getSuperClass()) != null && isAssignableFrom(sup);

    ASSIGNABLE_CACHE.put(clazz, assignable);
    return assignable;
  }

  @Override
  public boolean isAssignableTo(final MetaClass clazz) {
    return clazz.isAssignableFrom(this);
  }

  private static boolean _hasInterface(final MetaClass[] from, final MetaClass to) {
    for (final MetaClass interfaceType : from) {
      if (to.getFullyQualifiedName().equals(interfaceType.getErased().getFullyQualifiedName()))
        return true;
      else if (_hasInterface(interfaceType.getInterfaces(), to))
        return true;
    }

    return false;
  }

  @Override
  public boolean isAssignableFrom(final Class clazz) {
    return isAssignableFrom(MetaClassFactory.get(clazz));
  }

  @Override
  public boolean isAssignableTo(final Class clazz) {
    return isAssignableTo(MetaClassFactory.get(clazz));
  }

  @Override
  public boolean isDefaultInstantiable() {
    final MetaConstructor c = getConstructor(new MetaClass[0]);
    return c != null && c.isPublic();
  }

  @Override
  public MetaParameterizedType getParameterizedType() {
    return parameterizedType;
  }

  @Override
  public MetaParameterizedType getGenericSuperClass() {
    return genericSuperClass;
  }

  @Override
  public boolean equals(final Object o) {
    return o instanceof AbstractMetaClass && hashString().equals(((AbstractMetaClass) o).hashString());
  }

  @Override
  public int hashCode() {
    return hashString().hashCode();
  }

  private volatile transient Class<?> _asClassCache;

  @Override
  public Class<?> asClass() {
    if (_asClassCache != null) {
      return _asClassCache;
    }

    Class<?> cls = MetaClassFactory.PRIMITIVE_LOOKUP.get(getFullyQualifiedName());
    if (cls == null) {
      cls = NullType.class;
    }

    if (enclosedMetaObject instanceof Class) {
      cls = (Class<?>) enclosedMetaObject;
    }
    else if (isArray()) {
      try {
        cls = Class.forName(getInternalName().replaceAll("/", "\\."), false,
            Thread.currentThread().getContextClassLoader());
      }
      catch (ClassNotFoundException e) {
        e.printStackTrace();
        cls = null;
      }
    }
    else {
      try {
        cls = Thread.currentThread().getContextClassLoader().loadClass(getFullyQualifiedName());
      }
      catch (ClassNotFoundException e) {
        // ignore.
      }
    }

    return _asClassCache = cls;
  }

  private MetaClass _boxedCache;

  @Override
  public MetaClass asBoxed() {
    if (_boxedCache != null)
      return _boxedCache;
    return _boxedCache = GenUtil.getPrimitiveWrapper(this);
  }

  private MetaClass _unboxedCache;

  @Override
  public MetaClass asUnboxed() {
    if (_unboxedCache != null)
      return _unboxedCache;
    return _unboxedCache = GenUtil.getUnboxedFromWrapper(this);
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

  private Boolean _isPrimitiveWrapper;

  @Override
  public boolean isPrimitiveWrapper() {
    return _isPrimitiveWrapper != null ? _isPrimitiveWrapper : (_isPrimitiveWrapper = GenUtil.isPrimitiveWrapper(this));
  }

  private String _internalNameCache;

  @Override
  public String getInternalName() {
    if (_internalNameCache != null)
      return _internalNameCache;

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
      name = "L".concat(getInternalPrimitiveNameFrom(name.trim()).replaceAll("\\.", "/")).concat(";");
    }

    return _internalNameCache = dimString + name;
  }

  private static String getInternalPrimitiveNameFrom(final String name) {
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

  private static Class<?> getPrimitiveRefFrom(final String name) {
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
  public BeanDescriptor getBeanDescriptor() {
    return new BeanDescriptor() {
      private final Set<String> properties;
      private final Map<String, MetaMethod> getterProperties;
      private final Map<String, MetaMethod> setterProperties;

      {
        final Set<String> properties = new HashSet<String>();
        final Map<String, MetaMethod> getterProperties = new HashMap<String, MetaMethod>();
        final Map<String, MetaMethod> setterProperties = new HashMap<String, MetaMethod>();

        for (final MetaMethod method : getMethods()) {
          final String property = ReflectionUtil.getPropertyFromAccessor(method.getName());

          if (method.getParameters().length == 0
              && (method.getName().startsWith("get") || method.getName().startsWith("is"))) {
            properties.add(property);
            getterProperties.put(property, method);
          }
          else if (method.getParameters().length == 1 && method.getName().startsWith("set")) {
            properties.add(property);
            setterProperties.put(property, method);
          }
        }

        this.properties = Collections.unmodifiableSet(properties);
        this.getterProperties = Collections.unmodifiableMap(getterProperties);
        this.setterProperties = Collections.unmodifiableMap(setterProperties);
      }

      @Override
      public String getBeanName() {
        return Introspector.decapitalize(getName());
      }

      @Override
      public Set<String> getProperties() {
        return properties;
      }

      @Override
      public MetaMethod getReadMethodForProperty(final String propertyName) {
        return getterProperties.get(propertyName);
      }

      @Override
      public MetaMethod getWriteMethodForProperty(final String propertyName) {
        return setterProperties.get(propertyName);
      }

      @Override
      public MetaClass getPropertyType(String propertyName) {
        MetaMethod readMethod = getReadMethodForProperty(propertyName);
        if (readMethod != null) {
          return readMethod.getReturnType();
        }

        return getWriteMethodForProperty(propertyName).getParameters()[0].getType();
      }
    };
  }

  @Override
  public MetaClass getOuterComponentType() {
    if (_outerComponentCache != null)
      return _outerComponentCache;

    MetaClass c = this;
    while (c.isArray()) {
      c = c.getComponentType();
    }
    return _outerComponentCache = c;
  }

  @Override
  public String toString() {
    return getFullyQualifiedNameWithTypeParms();
  }
}
