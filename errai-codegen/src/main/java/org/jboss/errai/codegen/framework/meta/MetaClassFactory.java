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

package org.jboss.errai.codegen.framework.meta;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

import javax.enterprise.util.TypeLiteral;

import org.jboss.errai.codegen.framework.*;
import org.jboss.errai.codegen.framework.builder.callstack.LoadClassReference;
import org.jboss.errai.codegen.framework.meta.impl.build.*;
import org.jboss.errai.codegen.framework.meta.impl.gwt.GWTClass;
import org.jboss.errai.codegen.framework.meta.impl.java.JavaReflectionClass;
import org.jboss.errai.codegen.framework.util.EmptyStatement;
import org.jboss.errai.codegen.framework.util.GenUtil;
import org.mvel2.ConversionHandler;
import org.mvel2.DataConversion;

import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;

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

  private static final Map<String, MetaClass> CLASS_CACHE = new HashMap<String, MetaClass>();

  public static MetaClass get(String fullyQualifiedClassName) {
    return createOrGet(fullyQualifiedClassName);
  }

  public static MetaClass get(TypeOracle typeOracle, String fullyQualifiedClassName) {
    return createOrGet(typeOracle, fullyQualifiedClassName);
  }

  public static MetaClass get(TypeOracle typeOracle, Class<?> clazz) {
    return get(typeOracle, clazz.getName());
  }

  public static MetaClass get(JType clazz) {
    return createOrGet(clazz);
  }

  public static MetaClass get(Class<?> clazz) {
    return createOrGet(clazz);
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
    return createOrGet(Array.newInstance(clazz, dims).getClass());
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
    final MetaClass metaClass = createOrGet(clazz);
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
    return CLASS_CACHE.containsKey(name);
  }

  private static MetaClass createOrGet(String fullyQualifiedClassName) {
    if (!CLASS_CACHE.containsKey(fullyQualifiedClassName)) {
      return createOrGet(load(fullyQualifiedClassName));
    }

    return CLASS_CACHE.get(fullyQualifiedClassName);
  }

  private static MetaClass createOrGet(TypeOracle oracle, String fullyQualifiedClassName) {
    if (!CLASS_CACHE.containsKey(fullyQualifiedClassName)) {
      return createOrGet(load(oracle, fullyQualifiedClassName));
    }

    return CLASS_CACHE.get(fullyQualifiedClassName);
  }

  private static MetaClass createOrGet(TypeLiteral type) {
    if (type == null) return null;

    if (!CLASS_CACHE.containsKey(type.toString())) {
      MetaClass gwtClass = JavaReflectionClass.newUncachedInstance(type);

      addLookups(type, gwtClass);
      return gwtClass;
    }

    return CLASS_CACHE.get(type.toString());
  }


  private static MetaClass createOrGet(JType type) {
    if (type == null) return null;

    if (type.isParameterized() != null) {
      return GWTClass.newUncachedInstance(type);
    }

    if (!CLASS_CACHE.containsKey(type.getQualifiedSourceName())) {
      MetaClass gwtClass = GWTClass.newUncachedInstance(type);

      addLookups(type, gwtClass);
      return gwtClass;
    }

    return CLASS_CACHE.get(type.getQualifiedSourceName());
  }


  private static MetaClass createOrGet(Class cls) {
    if (cls == null) return null;

    if (cls.getTypeParameters() != null) {
      return JavaReflectionClass.newUncachedInstance(cls);
    }

    if (!CLASS_CACHE.containsKey(cls.getName())) {
      MetaClass javaReflectionClass = JavaReflectionClass.newUncachedInstance(cls);

      addLookups(cls, javaReflectionClass);
      return javaReflectionClass;
    }

    return CLASS_CACHE.get(cls.getName());
  }

  private static MetaClass createOrGet(Class cls, Type type) {
    if (cls == null) return null;

    if (cls.getTypeParameters() != null) {
      return JavaReflectionClass.newUncachedInstance(cls, type);
    }

    if (!CLASS_CACHE.containsKey(cls.getName())) {
      MetaClass javaReflectionClass = JavaReflectionClass.newUncachedInstance(cls, type);

      addLookups(cls, javaReflectionClass);
      return javaReflectionClass;
    }

    return CLASS_CACHE.get(cls.getName());
  }


  public static MetaClass parameterizedAs(Class clazz, MetaParameterizedType parameterizedType) {
    return parameterizedAs(MetaClassFactory.get(clazz), parameterizedType);
  }

  public static MetaClass parameterizedAs(MetaClass clazz, MetaParameterizedType parameterizedType) {
    BuildMetaClass buildMetaClass = new BuildMetaClass(Context.create());

    buildMetaClass.setReifiedFormOf(clazz);
    buildMetaClass.setClassName(clazz.getFullyQualifiedName());
    buildMetaClass.setAbstract(clazz.isAbstract());
    buildMetaClass.setFinal(clazz.isFinal());
    buildMetaClass.setInterface(clazz.isInterface());
    buildMetaClass.setInterfaces(Arrays.asList(clazz.getInterfaces()));
    buildMetaClass.setScope(GenUtil.scopeOf(clazz));
    buildMetaClass.setSuperClass(clazz.getSuperClass());

    for (MetaTypeVariable typeVariable : clazz.getTypeParameters()) {
      buildMetaClass.addTypeVariable(typeVariable);
    }

    buildMetaClass.settParameterizedType(parameterizedType);

    for (MetaField field : clazz.getFields()) {
      buildMetaClass.addField(new BuildMetaField(buildMetaClass, EmptyStatement.INSTANCE,
              GenUtil.scopeOf(field), field.getType(), field.getName()));
    }

    for (MetaConstructor c : clazz.getConstructors()) {
      BuildMetaConstructor newConstructor = new BuildMetaConstructor(buildMetaClass, EmptyStatement.INSTANCE,
                    GenUtil.scopeOf(c),
                    DefParameters.from(c));
      newConstructor.setReifiedFormOf(c);

      buildMetaClass.addConstructor(newConstructor);
    }

    for (MetaMethod method : clazz.getMethods()) {
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

      BuildMetaMethod newMethod = new BuildMetaMethod(buildMetaClass, EmptyStatement.INSTANCE,
                    GenUtil.scopeOf(method), GenUtil.modifiersOf(method), method.getName(), returnType,
                    method.getGenericReturnType(),
                    DefParameters.fromParameters(parameters), ThrowsDeclaration.of(method.getCheckedExceptions()));

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
    CLASS_CACHE.put(literal.toString(), metaClass);
  }

  private static void addLookups(Class cls, MetaClass metaClass) {
    CLASS_CACHE.put(cls.getName(), metaClass);
  }

  private static void addLookups(JType cls, MetaClass metaClass) {
    CLASS_CACHE.put(cls.getQualifiedSourceName(), metaClass);
  }

  private static Class<?> load(String fullyQualifiedName) {
    try {
      return Class.forName(fullyQualifiedName, false, Thread.currentThread().getContextClassLoader());
    }
    catch (ClassNotFoundException e) {
      throw new RuntimeException("Could not load class: " + fullyQualifiedName);
    }
  }

  private static JClassType load(TypeOracle oracle, String fullyQualifiedName) {
    try {
      return oracle.getType(fullyQualifiedName);
    }
    catch (NotFoundException e) {
      throw new RuntimeException("Could not load class: " + fullyQualifiedName);
    }
  }

  public static MetaClass[] fromClassArray(Class<?>[] classes) {
    MetaClass[] newClasses = new MetaClass[classes.length];
    for (int i = 0; i < classes.length; i++) {
      newClasses[i] = createOrGet(classes[i]);
    }
    return newClasses;
  }

  public static MetaClass[] fromClassArray(JClassType[] classes) {
    MetaClass[] newClasses = new MetaClass[classes.length];
    for (int i = 0; i < classes.length; i++) {
      newClasses[i] = createOrGet(classes[i]);
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
}
