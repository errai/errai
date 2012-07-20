package org.jboss.errai.codegen.util;

import com.google.gwt.core.client.UnsafeNativeLong;
import org.jboss.errai.codegen.Modifier;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaConstructor;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class with methods that generate code to access private, default
 * access ("package private"), and protected methods and fields in arbirtary
 * classes. Each generator allows the choice of generating Java Reflection code
 * (for use on the server side) or JSNI code (for use on the client side).
 *
 * @author Mike Brock
 * @author Jonathan Fuerth
 */
public class PrivateAccessUtil {
  private static final Map<String, PrivateMemberAccessor> PRIVATE_MEMBER_ACCESSORS
          = new HashMap<String, PrivateMemberAccessor>();

  static {
    PRIVATE_MEMBER_ACCESSORS.put("reflection", new ReflectionPrivateMemberAccessor());
  }

  public static void registerPrivateMemberAccessor(final String type, final PrivateMemberAccessor accessor) {
    PRIVATE_MEMBER_ACCESSORS.put(type, accessor);
  }

  /**
   * Annotation instance that can be passed to the code generator when generating long accessors.
   */
  private static final UnsafeNativeLong UNSAFE_NATIVE_LONG_ANNOTATION = new UnsafeNativeLong() {
    @Override
    public Class<? extends Annotation> annotationType() {
      return UnsafeNativeLong.class;
    }
  };

  public static void addPrivateAccessStubs(final String type,
                                           final ClassStructureBuilder<?> classBuilder,
                                           final MetaField f) {
    addPrivateAccessStubs(PrivateAccessType.Both, type, classBuilder, f);
  }

  /**
   * Generates methods for accessing a private field using either JSNI or Java
   * Reflection. The generated methods will be private and static.
   *
   * @param accessType
   *         Whether to generate a read method, a write method, or both.
   * @param type
   *         The type of accessors to use (ie. "reflection" or "jsni").
   * @param classBuilder
   *         The class builder to add the generated methods to.
   * @param f
   *         The field the generated accessors read and write.
   */
  public static void addPrivateAccessStubs(final PrivateAccessType accessType,
                                           final String type,
                                           final ClassStructureBuilder<?> classBuilder,
                                           final MetaField f) {
    addPrivateAccessStubs(accessType, type, classBuilder, f, new Modifier[]{Modifier.Static});
  }

  /**
   * Generates methods for accessing a private field using either JSNI or Java
   * Reflection. The generated methods will be private.
   *
   * @param accessType
   *         Whether to generate a read method, a write method, or both.
   * @param accessorType
   *         The type of accessors to use (ie. "reflection" or "jsni").
   * @param classBuilder
   *         The class builder to add the generated methods to.
   * @param f
   *         The field the generated accessors read and write.
   * @param modifiers
   *         The modifiers on the generated method, for example
   *         {@link Modifier#Final} or {@link Modifier#Synchronized}. <i>Never
   *         specify {@code Modifier.JSNI}</i>; it is added automatically when
   *         needed.
   */
  public static void addPrivateAccessStubs(final PrivateAccessType accessType,
                                           final String accessorType,
                                           final ClassStructureBuilder<?> classBuilder,
                                           final MetaField f,
                                           Modifier[] modifiers) {
    final MetaClass type = f.getType();

    final boolean read = accessType == PrivateAccessType.Read || accessType == PrivateAccessType.Both;
    final boolean write = accessType == PrivateAccessType.Write || accessType == PrivateAccessType.Both;

    final PrivateMemberAccessor privateMemberAccessor
            = PRIVATE_MEMBER_ACCESSORS.get(accessorType);

    if (privateMemberAccessor == null) {
      throw new IllegalArgumentException("unknown accessor type: " + accessorType);
    }

    if (read) {
      privateMemberAccessor.createReadableField(type, classBuilder, f, modifiers);
    }

    if (write) {
      privateMemberAccessor.createWritableField(type, classBuilder, f, modifiers);
    }
  }

  /**
   * Generates methods for accessing a nonpublic constructor using either JSNI or
   * Java Reflection. The generated method will be private and static. The name
   * of the generated method can be discovered by calling
   * {@link #getPrivateMethodName(MetaMethod)}.
   *
   * @param accessorType
   *         The type of accessors to use (ie. "reflection" or "jsni").
   * @param classBuilder
   *         The class builder to add the generated method to.
   * @param m
   *         The constructor the generated method will invoke
   */
  public static void addPrivateAccessStubs(final String accessorType,
                                           final ClassStructureBuilder<?> classBuilder,
                                           final MetaConstructor m) {

    final PrivateMemberAccessor privateMemberAccessor
            = PRIVATE_MEMBER_ACCESSORS.get(accessorType);

    if (privateMemberAccessor == null) {
      throw new IllegalArgumentException("unknown accessor type: " + accessorType);
    }

    privateMemberAccessor.makeConstructorAccessible(classBuilder, m);
  }

  /**
   * Generates methods for accessing a nonpublic method using either JSNI or
   * Java Reflection. The generated method will be private and static. The name
   * of the generated method can be discovered by calling
   * {@link #getPrivateMethodName(MetaMethod)}.
   *
   * @param accessorType
   *         The type of accessors to use (ie. "reflection" or "jsni").
   * @param classBuilder
   *         The class builder to add the generated method to.
   * @param m
   *         The nonpublic method the generated method will invoke
   */
  public static void addPrivateAccessStubs(final String accessorType,
                                           final ClassStructureBuilder<?> classBuilder,
                                           final MetaMethod m) {
    addPrivateAccessStubs(accessorType, classBuilder, m, new Modifier[]{Modifier.Static});
  }

  /**
   * Generates methods for accessing a nonpublic method using either JSNI or Java
   * Reflection. The generated method will be private and static.
   *
   * @param accessorType
   *         If true, the generated method will use JSNI to access the field.
   *         Otherwise, Java reflection will be used (in this case, the
   *         generated code will not be GWT translatable).
   * @param classBuilder
   *         The class builder to add the generated method to.
   * @param m
   *         The method the generated accessors read and write.
   * @param modifiers
   *         The modifiers on the generated method, for example
   *         {@link Modifier#Final} or {@link Modifier#Synchronized}. <i>Never
   *         specify {@code Modifier.JSNI}</i>; it is added automatically when
   *         needed.
   */
  public static void addPrivateAccessStubs(final String accessorType,
                                           final ClassStructureBuilder<?> classBuilder,
                                           final MetaMethod m,
                                           Modifier[] modifiers) {


    final PrivateMemberAccessor privateMemberAccessor
            = PRIVATE_MEMBER_ACCESSORS.get(accessorType);

    if (privateMemberAccessor == null) {
      throw new IllegalArgumentException("unknown accessor type: " + accessorType);
    }

    privateMemberAccessor.makeMethodAccessible(classBuilder, m, modifiers);
  }

  public static String getPrivateFieldInjectorName(final MetaField field) {
    return field.getDeclaringClass()
            .getFullyQualifiedName().replaceAll("\\.", "_") + "_" + field.getName();
  }

  public static String getPrivateMethodName(final MetaMethod method) {
    final StringBuilder buf = new StringBuilder(method.getDeclaringClass()
            .getFullyQualifiedName().replaceAll("\\.", "_") + "_" + method.getName());

    for (final MetaParameter parm : method.getParameters()) {
      buf.append('_').append(parm.getType().getFullyQualifiedName().replaceAll("\\.", "_"));
    }

    return buf.toString();
  }

  /**
   * Returns a new array consisting of a copy of the given array, plus
   * Modifiers.JSNI as the last element.
   *
   * @param modifiers
   *         The array to copy. May be empty, but must not be null.
   *
   * @return An array of length {@code n + 1}, where {@code n} is the length of
   *         the given array. Positions 0..n-1 correspond with the respective
   *         entries in the given array, and position n contains Modifiers.JSNI.
   */
  public static Modifier[] appendJsni(Modifier[] modifiers) {
    final Modifier[] origModifiers = modifiers;
    modifiers = new Modifier[origModifiers.length + 1];
    System.arraycopy(origModifiers, 0, modifiers, 0, origModifiers.length);
    modifiers[modifiers.length - 1] = Modifier.JSNI;
    return modifiers;
  }

  public static String getReflectionFieldGetterName(final MetaField f) {
    final MetaClass t = f.getType();

    if (!t.isPrimitive()) {
      return "get";
    }
    else if (t.getFullyQualifiedName().equals("int")) {
      return "getInt";
    }
    else if (t.getFullyQualifiedName().equals("short")) {
      return "getShort";
    }
    else if (t.getFullyQualifiedName().equals("boolean")) {
      return "getBoolean";
    }
    else if (t.getFullyQualifiedName().equals("double")) {
      return "getDouble";
    }
    else if (t.getFullyQualifiedName().equals("float")) {
      return "getFloat";
    }
    else if (t.getFullyQualifiedName().equals("byte")) {
      return "getByte";
    }
    else if (t.getFullyQualifiedName().equals("long")) {
      return "getLong";
    }
    else if (t.getFullyQualifiedName().equals("char")) {
      return "getChar";
    }
    return null;
  }

  public static String getReflectionFieldSetterName(final MetaField f) {
    final MetaClass t = f.getType();

    if (!t.isPrimitive()) {
      return "set";
    }
    else if (t.getFullyQualifiedName().equals("int")) {
      return "setInt";
    }
    else if (t.getFullyQualifiedName().equals("short")) {
      return "setShort";
    }
    else if (t.getFullyQualifiedName().equals("boolean")) {
      return "setBoolean";
    }
    else if (t.getFullyQualifiedName().equals("double")) {
      return "setDouble";
    }
    else if (t.getFullyQualifiedName().equals("float")) {
      return "setFloat";
    }
    else if (t.getFullyQualifiedName().equals("byte")) {
      return "setByte";
    }
    else if (t.getFullyQualifiedName().equals("long")) {
      return "setLong";
    }
    else if (t.getFullyQualifiedName().equals("char")) {
      return "setChar";
    }
    return null;
  }


}
