/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.codegen.util;

import org.apache.commons.lang3.AnnotationUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jboss.errai.codegen.meta.MetaAnnotation;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.RuntimeAnnotation;
import org.jboss.errai.codegen.meta.impl.apt.APTAnnotation;
import org.jboss.errai.common.metadata.MetaDataScanner;
import org.jboss.errai.common.metadata.ScannerSingleton;

import javax.enterprise.util.Nonbinding;
import javax.inject.Named;
import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * Helper methods for working with {@link Annotation} instances.
 * </p>
 *
 * <p>
 * This class contains various utility methods that make working with
 * annotations simpler.
 * </p>
 *
 * <p>
 * This modified version of {@link AnnotationUtils} ignores {@link Nonbinding}
 * values when calculating hashcodes and equality, for comparing CDI qualifiers.
 * </p>
 *
 * <p>
 * {@link Annotation} instances are always proxy objects; unfortunately dynamic
 * proxies cannot be depended upon to know how to implement certain methods in
 * the same manner as would be done by "natural" {@link Annotation}s. The
 * methods presented in this class can be used to avoid that possibility. It is
 * of course also possible for dynamic proxies to actually delegate their e.g.
 * {@link Annotation#equals(Object)}/{@link Annotation#hashCode()}/
 * {@link Annotation#toString()} implementations to {@link CDIAnnotationUtils}.
 * </p>
 *
 * <p>
 * #ThreadSafe#
 * </p>
 *
 */
public class CDIAnnotationUtils {

    /**
     * A style that prints annotations as recommended.
     */
    private static final ToStringStyle TO_STRING_STYLE = new ToStringStyle() {
        /** Serialization version */
        private static final long serialVersionUID = 1L;

        {
            setDefaultFullDetail(true);
            setArrayContentDetail(true);
            setUseClassName(true);
            setUseShortClassName(true);
            setUseIdentityHashCode(false);
            setContentStart("(");
            setContentEnd(")");
            setFieldSeparator(", ");
            setArrayStart("[");
            setArrayEnd("]");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected String getShortClassName(java.lang.Class<?> cls) {
            Class<? extends Annotation> annotationType = null;
            for (final Class<?> iface : ClassUtils.getAllInterfaces(cls)) {
                if (Annotation.class.isAssignableFrom(iface)) {
                    @SuppressWarnings("unchecked")
                    final
                    //because we just checked the assignability
                    Class<? extends Annotation> found = (Class<? extends Annotation>) iface;
                    annotationType = found;
                    break;
                }
            }
            return new StringBuilder(annotationType == null ? "" : annotationType.getName())
                    .insert(0, '@').toString();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void appendDetail(StringBuffer buffer, String fieldName, Object value) {
            if (value instanceof Annotation) {
                value = CDIAnnotationUtils.toString((Annotation) value);
            }
            super.appendDetail(buffer, fieldName, value);
        }

    };

    /**
     * <p>{@code AnnotationUtils} instances should NOT be constructed in
     * standard programming. Instead, the class should be used statically.</p>
     *
     * <p>This constructor is public to permit tools that require a JavaBean
     * instance to operate.</p>
     */
    public CDIAnnotationUtils() {
    }

    //-----------------------------------------------------------------------
    /**
     * <p>Checks if two annotations are equal using the criteria for equality
     * presented in the {@link Annotation#equals(Object)} API docs.</p>
     *
     * @param a1 the first Annotation to compare, {@code null} returns
     * {@code false} unless both are {@code null}
     * @param a2 the second Annotation to compare, {@code null} returns
     * {@code false} unless both are {@code null}
     * @return {@code true} if the two annotations are {@code equal} or both
     * {@code null}
     */
    public static boolean equals(Annotation a1, Annotation a2) {
        if (a1 == a2) {
            return true;
        }
        if (a1 == null || a2 == null) {
            return false;
        }
        final Class<? extends Annotation> type = a1.annotationType();
        final Class<? extends Annotation> type2 = a2.annotationType();
        Validate.notNull(type, "Annotation %s with null annotationType()", a1);
        Validate.notNull(type2, "Annotation %s with null annotationType()", a2);
        if (!type.equals(type2)) {
            return false;
        }
        try {
            for (final Method m : type.getDeclaredMethods()) {
                if (m.getParameterTypes().length == 0
                        && isValidAnnotationMemberType(m.getReturnType())
                        && !m.isAnnotationPresent(Nonbinding.class)) {
                    final Object v1 = m.invoke(a1);
                    final Object v2 = m.invoke(a2);
                    if (!memberEquals(m.getReturnType(), v1, v2)) {
                        return false;
                    }
                }
            }
        } catch (final IllegalAccessException ex) {
            return false;
        } catch (final InvocationTargetException ex) {
            return false;
        }
        return true;
    }

    /**
     * <p>Generate a hash code for the given annotation using the algorithm
     * presented in the {@link Annotation#hashCode()} API docs.</p>
     *
     * @param a the Annotation for a hash code calculation is desired, not
     * {@code null}
     * @return the calculated hash code
     * @throws RuntimeException if an {@code Exception} is encountered during
     * annotation member access
     * @throws IllegalStateException if an annotation method invocation returns
     * {@code null}
     */
    public static int hashCode(Annotation a) {
        int result = 0;
        final Class<? extends Annotation> type = a.annotationType();
        for (final Method m : type.getDeclaredMethods()) {
            if (!m.isAnnotationPresent(Nonbinding.class)) {
              try {
                final Object value = m.invoke(a);
                if (value == null) {
                  throw new IllegalStateException(
                          String.format("Annotation method %s returned null", m));
                }
                result += hashMember(m.getName(), value);
              } catch (final RuntimeException ex) {
                throw ex;
              } catch (final Exception ex) {
                throw new RuntimeException(ex);
              }
            }
        }
        return result;
    }

    /**
     * <p>Generate a string representation of an Annotation, as suggested by
     * {@link Annotation#toString()}.</p>
     *
     * @param a the annotation of which a string representation is desired
     * @return the standard string representation of an annotation, not
     * {@code null}
     */
    public static String toString(final Annotation a) {
        final ToStringBuilder builder = new ToStringBuilder(a, TO_STRING_STYLE);
        for (final Method m : a.annotationType().getDeclaredMethods()) {
            if (m.getParameterTypes().length > 0) {
                continue; //wtf?
            }
            try {
                builder.append(m.getName(), m.invoke(a));
            } catch (final RuntimeException ex) {
                throw ex;
            } catch (final Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        return builder.build();
    }

    /**
     * <p>Checks if the specified type is permitted as an annotation member.</p>
     *
     * <p>The Java language specification only permits certain types to be used
     * in annotations. These include {@link String}, {@link Class}, primitive
     * types, {@link Annotation}, {@link Enum}, and single-dimensional arrays of
     * these types.</p>
     *
     * @param type the type to check, {@code null}
     * @return {@code true} if the type is a valid type to use in an annotation
     */
    public static boolean isValidAnnotationMemberType(Class<?> type) {
        if (type == null) {
            return false;
        }
        if (type.isArray()) {
            type = type.getComponentType();
        }
        return type.isPrimitive() || type.isEnum() || type.isAnnotation()
                || String.class.equals(type) || Class.class.equals(type);
    }

    //besides modularity, this has the advantage of autoboxing primitives:
    /**
     * Helper method for generating a hash code for a member of an annotation.
     *
     * @param name the name of the member
     * @param value the value of the member
     * @return a hash code for this member
     */
    private static int hashMember(String name, Object value) {
        final int part1 = name.hashCode() * 127;
        if (value.getClass().isArray()) {
            return part1 ^ arrayMemberHash(value.getClass().getComponentType(), value);
        }
        if (value instanceof Annotation) {
            return part1 ^ hashCode((Annotation) value);
        } else if (value instanceof MetaAnnotation){
          return part1 ^ hashCode((MetaAnnotation) value);
        }
        return part1 ^ value.hashCode();
    }

    /**
     * Helper method for checking whether two objects of the given type are
     * equal. This method is used to compare the parameters of two annotation
     * instances.
     *
     * @param type the type of the objects to be compared
     * @param o1 the first object
     * @param o2 the second object
     * @return a flag whether these objects are equal
     */
    private static boolean memberEquals(Class<?> type, Object o1, Object o2) {
        if (o1 == o2) {
            return true;
        }
        if (o1 == null || o2 == null) {
            return false;
        }
        if (type.isArray()) {
            return arrayMemberEquals(type.getComponentType(), o1, o2);
        }
        if (type.isAnnotation()) {
            return equals((Annotation) o1, (Annotation) o2);
        }
        return o1.equals(o2);
    }

    /**
     * Helper method for comparing two objects of an array type.
     *
     * @param componentType the component type of the array
     * @param o1 the first object
     * @param o2 the second object
     * @return a flag whether these objects are equal
     */
    private static boolean arrayMemberEquals(Class<?> componentType, Object o1, Object o2) {
        if (componentType.isAnnotation()) {
            return annotationArrayMemberEquals((Annotation[]) o1, (Annotation[]) o2);
        }
        if (componentType.equals(Byte.TYPE)) {
            return Arrays.equals((byte[]) o1, (byte[]) o2);
        }
        if (componentType.equals(Short.TYPE)) {
            return Arrays.equals((short[]) o1, (short[]) o2);
        }
        if (componentType.equals(Integer.TYPE)) {
            return Arrays.equals((int[]) o1, (int[]) o2);
        }
        if (componentType.equals(Character.TYPE)) {
            return Arrays.equals((char[]) o1, (char[]) o2);
        }
        if (componentType.equals(Long.TYPE)) {
            return Arrays.equals((long[]) o1, (long[]) o2);
        }
        if (componentType.equals(Float.TYPE)) {
            return Arrays.equals((float[]) o1, (float[]) o2);
        }
        if (componentType.equals(Double.TYPE)) {
            return Arrays.equals((double[]) o1, (double[]) o2);
        }
        if (componentType.equals(Boolean.TYPE)) {
            return Arrays.equals((boolean[]) o1, (boolean[]) o2);
        }
        return Arrays.equals((Object[]) o1, (Object[]) o2);
    }

    /**
     * Helper method for comparing two arrays of annotations.
     *
     * @param a1 the first array
     * @param a2 the second array
     * @return a flag whether these arrays are equal
     */
    private static boolean annotationArrayMemberEquals(Annotation[] a1, Annotation[] a2) {
        if (a1.length != a2.length) {
            return false;
        }
        for (int i = 0; i < a1.length; i++) {
            if (!equals(a1[i], a2[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Helper method for generating a hash code for an array.
     *
     * @param componentType the component type of the array
     * @param o the array
     * @return a hash code for the specified array
     */
    private static int arrayMemberHash(Class<?> componentType, Object o) {
        if (componentType.equals(Byte.TYPE)) {
            return Arrays.hashCode((byte[]) o);
        }
        if (componentType.equals(Short.TYPE)) {
            return Arrays.hashCode((short[]) o);
        }
        if (componentType.equals(Integer.TYPE)) {
            return Arrays.hashCode((int[]) o);
        }
        if (componentType.equals(Character.TYPE)) {
            return Arrays.hashCode((char[]) o);
        }
        if (componentType.equals(Long.TYPE)) {
            return Arrays.hashCode((long[]) o);
        }
        if (componentType.equals(Float.TYPE)) {
            return Arrays.hashCode((float[]) o);
        }
        if (componentType.equals(Double.TYPE)) {
            return Arrays.hashCode((double[]) o);
        }
        if (componentType.equals(Boolean.TYPE)) {
            return Arrays.hashCode((boolean[]) o);
        }
        return Arrays.hashCode((Object[]) o);
    }

    public static String formatDefaultElName(final String rawName) {
      if (rawName.isEmpty() || Character.isLowerCase(rawName.charAt(0)) || (rawName.length() > 1 && Character.isUpperCase(rawName.charAt(1)))) {
        return rawName;
      } else {
        final StringBuilder builder = new StringBuilder(rawName);
        builder.setCharAt(0, Character.toLowerCase(builder.charAt(0)));

        return builder.toString();
      }
    }

    public static Set<Class<?>> getQualifiersAsClasses() {
      final MetaDataScanner scanner = ScannerSingleton.getOrCreateInstance();
      final Set<Class<?>> typesAnnotatedWith = scanner.getTypesAnnotatedWith(Qualifier.class);
      typesAnnotatedWith.add(Named.class);
      return typesAnnotatedWith;
    }

    public static Set<MetaClass> getQualifiers() {
      final Set<Class<?>> qualifiersAsClasses = getQualifiersAsClasses();
      final Set<MetaClass> qualifiersAsMetaClasses = qualifiersAsClasses.stream().map(MetaClassFactory::get).collect(Collectors.toSet());

      if (qualifiersAsClasses.size() > qualifiersAsMetaClasses.size()) {
        throw new RuntimeException("Lost some qualifiers when converting from Class to MetaClass");
      }

      return qualifiersAsMetaClasses;
    }



  public static Collection<MetaMethod> getAnnotationAttributes(final MetaClass annoClass) {
    return Arrays.stream(annoClass.getDeclaredMethods())
            .filter(CDIAnnotationUtils::relevantForSerialization)
            .collect(Collectors.toList());
  }

  public static boolean relevantForSerialization(final MetaMethod method) {
    return !method.isAnnotationPresent(Nonbinding.class)
            && method.isPublic()
            && !method.getName().equals("equals")
            && !method.getName().equals("hashCode");
  }

  public static boolean equals(final MetaAnnotation anno1, final MetaAnnotation anno2) {

    if (anno1 instanceof RuntimeAnnotation && anno2 instanceof RuntimeAnnotation) {
      return equals(((RuntimeAnnotation) anno1).getAnnotation(), ((RuntimeAnnotation) anno2).getAnnotation());
    }

    if (anno1 instanceof APTAnnotation && anno2 instanceof APTAnnotation) {
      for (Map.Entry<String, Object> e : anno1.values().entrySet()) {
        final Object o = anno2.values().get(e.getKey());
        if (o == null || !o.equals(e.getValue())) {
          return false;
        }
      }
    }

    final MetaAnnotation apt = anno1 instanceof APTAnnotation ? anno1 : anno2;
    final MetaAnnotation runtime = anno2 instanceof APTAnnotation ? anno1 : anno2;

    if (apt instanceof APTAnnotation && runtime instanceof RuntimeAnnotation) {
      for (Map.Entry<String, Object> entry : apt.values().entrySet()) {
        final String key = entry.getKey();
        if (runtime.annotationType().getMethod(key, new MetaClass[0]).isAnnotationPresent(Nonbinding.class)) {
          continue;
        }
        Object runtimeValue = runtime.value(entry.getKey());
        Object aptValue = apt.value(entry.getKey());
        if (runtimeValue == null || !runtimeValue.equals(aptValue)) {
          return false;
        }
      }
    }

    return true;
  }


  public static int hashCode(final MetaAnnotation a) {
    if (a instanceof RuntimeAnnotation) {
      return hashCode(((RuntimeAnnotation) a).getAnnotation());
    }

    int result = 0;
    final MetaClass type = a.annotationType();
    for (final MetaMethod m : type.getDeclaredMethods()) {
      if (!m.isAnnotationPresent(Nonbinding.class)) {
        final Object value = a.value(m.getName());
        if (value == null) {
          throw new IllegalStateException(String.format("Annotation method %s returned null", m));
        }
        result += hashMember(m.getName(), value);
      }
    }
    return result;
  }
}
