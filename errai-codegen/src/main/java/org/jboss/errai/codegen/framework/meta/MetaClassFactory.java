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

package org.jboss.errai.codegen.framework.meta;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.util.TypeLiteral;

import org.jboss.errai.codegen.framework.Context;
import org.jboss.errai.codegen.framework.DefParameters;
import org.jboss.errai.codegen.framework.Parameter;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.ThrowsDeclaration;
import org.jboss.errai.codegen.framework.builder.callstack.LoadClassReference;
import org.jboss.errai.codegen.framework.meta.impl.build.BuildMetaClass;
import org.jboss.errai.codegen.framework.meta.impl.build.BuildMetaConstructor;
import org.jboss.errai.codegen.framework.meta.impl.build.BuildMetaField;
import org.jboss.errai.codegen.framework.meta.impl.build.BuildMetaMethod;
import org.jboss.errai.codegen.framework.meta.impl.build.BuildMetaParameterizedType;
import org.jboss.errai.codegen.framework.meta.impl.build.ShadowBuildMetaField;
import org.jboss.errai.codegen.framework.meta.impl.build.ShadowBuildMetaMethod;
import org.jboss.errai.codegen.framework.meta.impl.java.JavaReflectionClass;
import org.jboss.errai.codegen.framework.util.EmptyStatement;
import org.jboss.errai.codegen.framework.util.GenUtil;
import org.mvel2.ConversionHandler;
import org.mvel2.DataConversion;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public final class MetaClassFactory {
  static {
    DataConversion.addConversionHandler(Class.class, new ConversionHandler() {
      @Override
      public Object convertFrom(Object in) {
        if (MetaClass.class.isAssignableFrom(in.getClass())) {
          return ((MetaClass) in).asClass();
        }
        else {
          throw new RuntimeException("cannot convert from " + in.getClass() + "; to " + Class.class.getName());
        }
      }

      @Override
      public boolean canConvertFrom(Class cls) {
        return MetaType.class.isAssignableFrom(cls);
      }
    });

    DataConversion.addConversionHandler(Class[].class, new ConversionHandler() {
      @Override
      public Object convertFrom(Object in) {
        int length = Array.getLength(in);

        Class[] cls = new Class[length];
        for (int i = 0; i < length; i++) {
          Object el = Array.get(in, i);

          if (el instanceof MetaClass) {
            cls[i] = ((MetaClass) el).asClass();
          }
          else if (el != null && el.getClass().isArray()) {
            cls[i] = (Class) convertFrom(el);
          }
          else {
            cls[i] = null;
          }
        }
        return cls;
      }

      @Override
      public boolean canConvertFrom(Class cls) {
        while (cls.isArray()) {
          cls = cls.getComponentType();
        }
        return MetaClass.class.isAssignableFrom(cls) || MetaType.class.isAssignableFrom(cls);
      }
    });
  }

  private static final Map<String, MetaClass> PRIMARY_CLASS_CACHE = new HashMap<String, MetaClass>(1000);
  private static final Map<String, MetaClass> ERASED_CLASS_CACHE = new HashMap<String, MetaClass>(1000);
  // private static final Map<Class, MetaClass> CLASS_CACHE = new HashMap<Class, MetaClass>();

  public static void pushCache(MetaClass clazz) {
    PRIMARY_CLASS_CACHE.put(clazz.getFullyQualifiedName(), clazz);
  }

  public static MetaClass get(String fullyQualifiedClassName, boolean erased) {
    return createOrGet(fullyQualifiedClassName, erased);
  }

  public static MetaClass get(String fullyQualifiedClassName) {
    return createOrGet(fullyQualifiedClassName);
  }

  public static MetaClass get(Class<?> clazz) {
    if (clazz == null) return null;
    return createOrGet(clazz.getName(), false);
  }

  public static MetaClass getArrayOf(Class<?> clazz, int dims) {
    int[] da = new int[dims];
    for (int i = 0; i < da.length; i++) {
      da[i] = 0;
    }

    return getArrayOf(clazz, da);
  }

  public static MetaClass getArrayOf(Class<?> clazz, int... dims) {
    if (dims.length == 0) {
      dims = new int[1];
    }
    return JavaReflectionClass.newInstance(Array.newInstance(clazz, dims).getClass());
  }

  public static MetaClass get(Class<?> clazz, Type type) {
    return createOrGet(clazz, type);
  }

  public static MetaClass get(TypeLiteral<?> literal) {
    return createOrGet(literal);
  }

  public static MetaMethod get(Method method) {
    return get(method.getDeclaringClass()).getDeclaredMethod(method.getName(), method.getParameterTypes());
  }

  public static MetaField get(Field field) {
    return get(field.getDeclaringClass()).getDeclaredField(field.getName());
  }

  public static Statement getAsStatement(Class<?> clazz) {
    final MetaClass metaClass = createOrGet(clazz.getName(), false);
    return new Statement() {
      @Override
      public String generate(Context context) {
        return LoadClassReference.getClassReference(metaClass, context);
      }

      @Override
      public MetaClass getType() {
        return MetaClassFactory.get(Class.class);
      }

      public Context getContext() {
        return null;
      }
    };
  }

  public static boolean isCached(String name) {
    return ERASED_CLASS_CACHE.containsKey(name);
  }

  private static MetaClass createOrGet(String fullyQualifiedClassName) {
    if (!ERASED_CLASS_CACHE.containsKey(fullyQualifiedClassName)) {
      return createOrGet(fullyQualifiedClassName, false);
    }

    return ERASED_CLASS_CACHE.get(fullyQualifiedClassName);
  }

//  private static MetaClass createOrGet(TypeOracle oracle, String fullyQualifiedClassName) {
//    if (!ERASED_CLASS_CACHE.containsKey(fullyQualifiedClassName)) {
//      return createOrGet(load(oracle, fullyQualifiedClassName));
//    }
//
//    return ERASED_CLASS_CACHE.get(fullyQualifiedClassName);
//  }

  private static MetaClass createOrGet(TypeLiteral type) {
    if (type == null) return null;

    if (!ERASED_CLASS_CACHE.containsKey(type.toString())) {
      MetaClass gwtClass = JavaReflectionClass.newUncachedInstance(type);

      addLookups(type, gwtClass);
      return gwtClass;
    }

    return ERASED_CLASS_CACHE.get(type.toString());
  }


//  private static MetaClass createOrGet(JType type) {
//    if (type == null) return null;
//
//    if (type.isParameterized() != null) {
//      return GWTClass.newUncachedInstance(type);
//    }
//
//    if (!ERASED_CLASS_CACHE.containsKey(type.getQualifiedSourceName())) {
//      MetaClass gwtClass = GWTClass.newUncachedInstance(type, true);
//
//      addLookups(type, gwtClass);
//      return gwtClass;
//    }
//
//    return ERASED_CLASS_CACHE.get(type.getQualifiedSourceName());
//  }


  private static MetaClass createOrGet(String clsName, boolean erased) {
    if (clsName == null) return null;

    MetaClass mCls;
    if (erased) {
      mCls = ERASED_CLASS_CACHE.get(clsName);
      if (mCls == null) {
        ERASED_CLASS_CACHE.put(clsName, mCls = JavaReflectionClass.newUncachedInstance(loadClass(clsName), erased));
      }
    }
    else {
      mCls = PRIMARY_CLASS_CACHE.get(clsName);
      if (mCls == null) {
        PRIMARY_CLASS_CACHE.put(clsName, mCls = JavaReflectionClass.newUncachedInstance(loadClass(clsName), erased));
      }
    }
    return mCls;
  }

  private static MetaClass createOrGet(Class cls, Type type) {
    if (cls == null) return null;

    if (cls.getTypeParameters() != null) {
      return JavaReflectionClass.newUncachedInstance(cls, type);
    }

    if (!ERASED_CLASS_CACHE.containsKey(cls.getName())) {
      MetaClass javaReflectionClass = JavaReflectionClass.newUncachedInstance(cls, type);

      addLookups(cls, javaReflectionClass);
      return javaReflectionClass;
    }

    return ERASED_CLASS_CACHE.get(cls.getName());
  }


  public static MetaClass parameterizedAs(Class clazz, MetaParameterizedType parameterizedType) {
    return parameterizedAs(MetaClassFactory.get(clazz), parameterizedType);
  }

  public static MetaClass parameterizedAs(MetaClass clazz, MetaParameterizedType parameterizedType) {
    return cloneToBuildMetaClass(clazz, parameterizedType);
  }

  public static MetaClass erasedVersionOf(MetaClass clazz) {
    BuildMetaClass mc = cloneToBuildMetaClass(clazz);
    mc.setParameterizedType(null);
    return mc;
  }

  private static BuildMetaClass cloneToBuildMetaClass(MetaClass clazz) {
    return cloneToBuildMetaClass(clazz, null);
  }

  private static BuildMetaClass cloneToBuildMetaClass(MetaClass clazz, MetaParameterizedType parameterizedType) {
    BuildMetaClass buildMetaClass = new BuildMetaClass(Context.create(), clazz.getFullyQualifiedName());

    buildMetaClass.setReifiedFormOf(clazz);
    buildMetaClass.setAbstract(clazz.isAbstract());
    buildMetaClass.setFinal(clazz.isFinal());
    buildMetaClass.setStatic(clazz.isStatic());
    buildMetaClass.setInterface(clazz.isInterface());
    buildMetaClass.setInterfaces(Arrays.asList(clazz.getInterfaces()));
    buildMetaClass.setScope(GenUtil.scopeOf(clazz));
    buildMetaClass.setSuperClass(clazz.getSuperClass());

    for (MetaTypeVariable typeVariable : clazz.getTypeParameters()) {
      buildMetaClass.addTypeVariable(typeVariable);
    }

    if (parameterizedType != null) {
      buildMetaClass.setParameterizedType(parameterizedType);
    }
    else {
      buildMetaClass.setParameterizedType(clazz.getParameterizedType());
    }

    for (MetaField field : clazz.getDeclaredFields()) {
      BuildMetaField bmf = new ShadowBuildMetaField(buildMetaClass, EmptyStatement.INSTANCE,
              GenUtil.scopeOf(field), field.getType(), field.getName(), field);

      bmf.setFinal(field.isFinal());
      bmf.setStatic(field.isStatic());
      bmf.setVolatile(field.isVolatile());
      bmf.setTransient(field.isTransient());

      buildMetaClass.addField(bmf);
    }

    for (MetaConstructor c : clazz.getDeclaredConstructors()) {
      BuildMetaConstructor newConstructor = new BuildMetaConstructor(buildMetaClass, EmptyStatement.INSTANCE,
              GenUtil.scopeOf(c),
              DefParameters.from(c));
      newConstructor.setReifiedFormOf(c);

      buildMetaClass.addConstructor(newConstructor);
    }

    for (MetaMethod method : clazz.getDeclaredMethods()) {
      MetaClass returnType = method.getReturnType();
      if (method.getGenericReturnType() instanceof MetaTypeVariable) {
        MetaTypeVariable typeVariable = (MetaTypeVariable) method.getGenericReturnType();
        MetaClass tVarVal = getTypeVariableValue(typeVariable, buildMetaClass);
        if (tVarVal != null) {
          returnType = tVarVal;
        }
      }

      List<Parameter> parameters = new ArrayList<Parameter>();
      int i = 0;
      for (MetaParameter parm : method.getParameters()) {
        MetaClass parmType = null;
        if (method.getGenericParameterTypes() != null) {
          if (method.getGenericParameterTypes()[i] instanceof MetaTypeVariable) {
            MetaTypeVariable typeVariable = (MetaTypeVariable) method.getGenericParameterTypes()[i];

            MetaClass tVarVal = getTypeVariableValue(typeVariable, buildMetaClass);
            if (tVarVal != null) {
              parmType = tVarVal;
            }
          }
        }

        if (parmType == null) {
          parmType = parm.getType();
        }

        parameters.add(Parameter.of(parmType, parm.getName()));
        i++;
      }

      BuildMetaMethod newMethod = new ShadowBuildMetaMethod(buildMetaClass, EmptyStatement.INSTANCE,
              GenUtil.scopeOf(method), GenUtil.modifiersOf(method), method.getName(), returnType,
              method.getGenericReturnType(),
              DefParameters.fromParameters(parameters), ThrowsDeclaration.of(method.getCheckedExceptions()), method);

      newMethod.setReifiedFormOf(method);

      buildMetaClass.addMethod(newMethod);
    }

    return buildMetaClass;
  }

  private static MetaClass getTypeVariableValue(MetaTypeVariable typeVariable, MetaClass clazz) {
    int idx = -1;
    MetaTypeVariable[] typeVariables = clazz.getTypeParameters();
    for (int i = 0; i < typeVariables.length; i++) {
      if (typeVariables[i].getName().equals(typeVariable.getName())) {
        idx = i;
        break;
      }
    }

    if (idx != -1) {
      MetaType type = clazz.getParameterizedType().getTypeParameters()[idx];
      if (type instanceof MetaClass) {
        return (MetaClass) type;
      }
    }
    return null;
  }

  public static MetaParameterizedType typeParametersOf(Object... classes) {
    MetaType[] types = new MetaType[classes.length];
    int i = 0;
    for (Object o : classes) {
      if (o instanceof Class) {
        types[i++] = MetaClassFactory.get((Class) o);
      }
      else if (o instanceof TypeLiteral) {
        types[i++] = MetaClassFactory.get((TypeLiteral) o);
      }
      else if (o instanceof MetaType) {
        types[i++] = (MetaType) o;
      }
      else {
        throw new RuntimeException("not a recognized type reference: " + o.getClass().getName());
      }
    }

    return typeParametersOf(types);
  }

  public static MetaParameterizedType typeParametersOf(Class<?>... classes) {
    return typeParametersOf(fromClassArray(classes));
  }

  public static MetaParameterizedType typeParametersOf(MetaType... classes) {
    BuildMetaParameterizedType buildMetaParms = new BuildMetaParameterizedType(classes, null, null);

    return buildMetaParms;
  }

  private static void addLookups(TypeLiteral literal, MetaClass metaClass) {
    ERASED_CLASS_CACHE.put(literal.toString(), metaClass);
  }

  private static void addLookups(Class cls, MetaClass metaClass) {
    ERASED_CLASS_CACHE.put(cls.getName(), metaClass);
  }

//  private static void addLookups(JType cls, MetaClass metaClass) {
//    ERASED_CLASS_CACHE.put(cls.getQualifiedSourceName(), metaClass);
//  }

  private static void addLookups(String encName, MetaClass metaClass) {
    ERASED_CLASS_CACHE.put(encName, metaClass);
  }


  private static Map<String, Class<?>> PRIMITIVE_LOOKUP = new HashMap<String, Class<?>>() {
    {
      put("void", void.class);
      put("boolean", boolean.class);
      put("int", int.class);
      put("long", long.class);
      put("float", float.class);
      put("double", double.class);
      put("short", short.class);
      put("byte", byte.class);
      put("char", char.class);
    }
  };

  public static Class<?> loadClass(String fullyQualifiedName) {
    try {
      Class<?> cls = PRIMITIVE_LOOKUP.get(fullyQualifiedName);
      if (cls == null) {
        cls = Class.forName(fullyQualifiedName, false, Thread.currentThread().getContextClassLoader());
      }
      return cls;
    }
    catch (ClassNotFoundException e) {
      throw new RuntimeException("Could not load class: " + fullyQualifiedName);
    }
  }


  public static MetaClass[] fromClassArray(Class<?>[] classes) {
    MetaClass[] newClasses = new MetaClass[classes.length];
    for (int i = 0; i < classes.length; i++) {
      newClasses[i] = createOrGet(classes[i].getName(), false);
    }
    return newClasses;
  }

  public static Class<?>[] asClassArray(MetaParameter[] parms) {
    MetaType[] type = new MetaType[parms.length];
    for (int i = 0; i < parms.length; i++) {
      type[i] = parms[i].getType();
    }
    return asClassArray(type);
  }

  public static Class<?>[] asClassArray(MetaType[] cls) {
    Class<?>[] newClasses = new Class<?>[cls.length];
    for (int i = 0; i < cls.length; i++) {
      if (cls[i] instanceof MetaParameterizedType) {
        newClasses[i] = ((MetaClass) ((MetaParameterizedType) cls[i]).getRawType()).asClass();
      }
      else {
        newClasses[i] = ((MetaClass) cls[i]).asClass();
      }
    }
    return newClasses;
  }

  public static void emptyCache() {
    PRIMARY_CLASS_CACHE.clear();
    ERASED_CLASS_CACHE.clear();
  }
}
