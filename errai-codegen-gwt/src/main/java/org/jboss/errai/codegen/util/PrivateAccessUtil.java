package org.jboss.errai.codegen.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.codegen.Cast;
import org.jboss.errai.codegen.DefParameters;
import org.jboss.errai.codegen.Modifier;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.StringStatement;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.CatchBlockBuilder;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.ContextualStatementBuilder;
import org.jboss.errai.codegen.builder.MethodBlockBuilder;
import org.jboss.errai.codegen.builder.MethodCommentBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaConstructor;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;

import com.google.gwt.core.client.UnsafeNativeLong;

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
  private static final String JAVA_REFL_FLD_UTIL_METH = "_getAccessibleField";
  private static final String JAVA_REFL_METH_UTIL_METH = "_getAccessibleMethod";
  private static final String JAVA_REFL_CONSTRUCTOR_UTIL_METH = "_getAccessibleConstructor";

  /**
   * Annotation instance that can be passed to the code generator when generating long accessors.
   */
  private static final UnsafeNativeLong UNSAFE_NATIVE_LONG_ANNOTATION = new UnsafeNativeLong() {
    @Override
    public Class<? extends Annotation> annotationType() {
      return UnsafeNativeLong.class;
    }
  };

  public static void createJavaReflectionFieldInitializerUtilMethod(final ClassStructureBuilder<?> classBuilder) {

    if (classBuilder.getClassDefinition().getMethod(JAVA_REFL_FLD_UTIL_METH, Class.class, Field.class) != null) {
      return;
    }

    classBuilder.privateMethod(Field.class, JAVA_REFL_FLD_UTIL_METH).modifiers(Modifier.Static)
            .parameters(DefParameters.of(Parameter.of(Class.class, "cls"), Parameter.of(String.class, "name")))
            .body()
            ._(Stmt.try_()
                    ._(Stmt.declareVariable("fld", Stmt.loadVariable("cls").invoke("getDeclaredField",
                            Stmt.loadVariable("name"))))
                    ._(Stmt.loadVariable("fld").invoke("setAccessible", true))
                    ._(Stmt.loadVariable("fld").returnValue())
                    .finish()
                    .catch_(Throwable.class, "e")
                    ._(Stmt.loadVariable("e").invoke("printStackTrace"))
                    ._(Stmt.throw_(RuntimeException.class, Refs.get("e")))
                    .finish())
            .finish();
  }

  public static void createJavaReflectionMethodInitializerUtilMethod(
          final ClassStructureBuilder<?> classBuilder) {

    if (classBuilder.getClassDefinition().getMethod(JAVA_REFL_METH_UTIL_METH, Class.class, String.class, Class[].class) != null) {
      return;
    }

    classBuilder.privateMethod(Method.class, JAVA_REFL_METH_UTIL_METH).modifiers(Modifier.Static)
            .parameters(DefParameters.of(Parameter.of(Class.class, "cls"), Parameter.of(String.class, "name"),
                    Parameter.of(Class[].class, "parms")))
            .body()
            ._(Stmt.try_()
                    ._(Stmt.declareVariable("meth", Stmt.loadVariable("cls").invoke("getDeclaredMethod",
                            Stmt.loadVariable("name"), Stmt.loadVariable("parms"))))
                    ._(Stmt.loadVariable("meth").invoke("setAccessible", true))
                    ._(Stmt.loadVariable("meth").returnValue())
                    .finish()
                    .catch_(Throwable.class, "e")
                    ._(Stmt.loadVariable("e").invoke("printStackTrace"))
                    ._(Stmt.throw_(RuntimeException.class, Refs.get("e")))
                    .finish())
            .finish();
  }

  public static void createJavaReflectionConstructorInitializerUtilMethod(
          final ClassStructureBuilder<?> classBuilder) {

    if (classBuilder.getClassDefinition().getMethod(JAVA_REFL_CONSTRUCTOR_UTIL_METH, Class.class,
            Class[].class) != null) {
      return;
    }

    classBuilder.privateMethod(Constructor.class, JAVA_REFL_CONSTRUCTOR_UTIL_METH).modifiers(Modifier.Static)
            .parameters(DefParameters.of(Parameter.of(Class.class, "cls"), Parameter.of(Class[].class, "parms")))
            .body()
            ._(Stmt.try_()
                    ._(Stmt.declareVariable("cons", Stmt.loadVariable("cls").invoke("getDeclaredConstructor",
                            Stmt.loadVariable("parms"))))
                    ._(Stmt.loadVariable("cons").invoke("setAccessible", true))
                    ._(Stmt.loadVariable("cons").returnValue())
                    .finish()
                    .catch_(Throwable.class, "e")
                    ._(Stmt.loadVariable("e").invoke("printStackTrace"))
                    ._(Stmt.throw_(RuntimeException.class, Refs.get("e")))
                    .finish())
            .finish();
  }

  public static String initCachedField(final ClassStructureBuilder<?> classBuilder, final MetaField f) {
    createJavaReflectionFieldInitializerUtilMethod(classBuilder);

    final String fieldName = getPrivateFieldInjectorName(f) + "_fld";

    classBuilder.privateField(fieldName, Field.class).modifiers(Modifier.Static)
            .initializesWith(Stmt.invokeStatic(classBuilder.getClassDefinition(), JAVA_REFL_FLD_UTIL_METH,
                    f.getDeclaringClass(), f.getName())).finish();

    return fieldName;
  }

  public static String initCachedMethod(final ClassStructureBuilder<?> classBuilder, final MetaMethod m) {
    createJavaReflectionMethodInitializerUtilMethod(classBuilder);

    final String fieldName = getPrivateMethodName(m) + "_meth";

    classBuilder.privateField(fieldName, Method.class).modifiers(Modifier.Static)
            .initializesWith(Stmt.invokeStatic(classBuilder.getClassDefinition(), JAVA_REFL_METH_UTIL_METH,
                    m.getDeclaringClass(), m.getName(), MetaClassFactory.asClassArray(m.getParameters()))).finish();

    return fieldName;
  }

  public static String initCachedMethod(final ClassStructureBuilder<?> classBuilder, final MetaConstructor c) {
    createJavaReflectionConstructorInitializerUtilMethod(classBuilder);

    final String fieldName = getPrivateMethodName(c) + "_meth";

    classBuilder.privateField(fieldName, Constructor.class).modifiers(Modifier.Static)
            .initializesWith(Stmt.invokeStatic(classBuilder.getClassDefinition(), JAVA_REFL_CONSTRUCTOR_UTIL_METH,
                    c.getDeclaringClass(), MetaClassFactory.asClassArray(c.getParameters()))).finish();

    return fieldName;
  }

  public static void addPrivateAccessStubs(final boolean useJSNIStubs,
                                           final ClassStructureBuilder<?> classBuilder,
                                           final MetaField f) {
    addPrivateAccessStubs(PrivateAccessType.Both, useJSNIStubs, classBuilder, f);
  }

  /**
   * Generates methods for accessing a private field using either JSNI or Java
   * Reflection. The generated methods will be private and static.
   *
   * @param accessType
   *         Whether to generate a read method, a write method, or both.
   * @param useJSNIStubs
   *         If true, the generated methods will use JSNI to access the field.
   *         Otherwise, Java reflection will be used (in this case, the
   *         generated code will not be GWT translatable).
   * @param classBuilder
   *         The class builder to add the generated methods to.
   * @param f
   *         The field the generated accessors read and write.
   */
  public static void addPrivateAccessStubs(final PrivateAccessType accessType,
                                           final boolean useJSNIStubs,
                                           final ClassStructureBuilder<?> classBuilder,
                                           final MetaField f) {
    addPrivateAccessStubs(accessType, useJSNIStubs, classBuilder, f, new Modifier[]{Modifier.Static});
  }

  /**
   * Generates methods for accessing a private field using either JSNI or Java
   * Reflection. The generated methods will be private.
   *
   * @param accessType
   *         Whether to generate a read method, a write method, or both.
   * @param useJSNIStubs
   *         If true, the generated methods will use JSNI to access the field.
   *         Otherwise, Java reflection will be used (in this case, the
   *         generated code will not be GWT translatable).
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
                                           final boolean useJSNIStubs,
                                           final ClassStructureBuilder<?> classBuilder,
                                           final MetaField f,
                                           Modifier[] modifiers) {
    final MetaClass type = f.getType();

    final boolean read = accessType == PrivateAccessType.Read || accessType == PrivateAccessType.Both;
    final boolean write = accessType == PrivateAccessType.Write || accessType == PrivateAccessType.Both;

    if (useJSNIStubs) {

      modifiers = appendJsni(modifiers);

      if (write) {
        final MethodCommentBuilder<? extends ClassStructureBuilder<?>> methodBuilder =
                classBuilder.privateMethod(void.class, getPrivateFieldInjectorName(f));

        if (type.getCanonicalName().equals("long")) {
          methodBuilder.annotatedWith(UNSAFE_NATIVE_LONG_ANNOTATION);
        }

        if (!f.isStatic()) {
          methodBuilder
                  .parameters(DefParameters.fromParameters(Parameter.of(f.getDeclaringClass(), "instance"),
                          Parameter.of(type, "value")));
        }

        methodBuilder.modifiers(modifiers)
                .body()
                ._(new StringStatement(JSNIUtil.fieldAccess(f) + " = value"))
                .finish();
      }

      if (read) {
        final MethodBlockBuilder<? extends ClassStructureBuilder<?>> instance =
                classBuilder.privateMethod(type, getPrivateFieldInjectorName(f));

        if (!f.isStatic()) {
          instance.parameters(DefParameters.fromParameters(Parameter.of(f.getDeclaringClass(), "instance")));
        }

        if (type.getCanonicalName().equals("long")) {
          instance.annotatedWith(UNSAFE_NATIVE_LONG_ANNOTATION);
        }

        instance.modifiers(modifiers)
                .body()
                ._(new StringStatement("return " + JSNIUtil.fieldAccess(f)))
                .finish();
      }
    }
    else {

      /*
       * Reflection stubs
       */

      final String cachedField = initCachedField(classBuilder, f);
      final String setterName = _getReflectionFieldMethSetName(f);

      if (write) {
        final MethodCommentBuilder<? extends ClassStructureBuilder<?>> methodBuilder =
                classBuilder.privateMethod(void.class, getPrivateFieldInjectorName(f));

        if (!f.isStatic()) {
          methodBuilder
                  .parameters(DefParameters.fromParameters(Parameter.of(f.getDeclaringClass(), "instance"),
                          Parameter.of(f.getType(), "value")));
        }

        methodBuilder.modifiers(modifiers)
                .body()
                ._(Stmt.try_()
                        ._(Stmt.loadVariable(cachedField).invoke(setterName, Refs.get("instance"), Refs.get("value")))
                        .finish()
                        .catch_(Throwable.class, "e")
                        ._(Stmt.loadVariable("e").invoke("printStackTrace"))
                        ._(Stmt.throw_(RuntimeException.class, Refs.get("e")))
                        .finish())
                .finish();
      }

      final String getterName = _getReflectionFieldMethGetName(f);

      if (read) {
        final MethodCommentBuilder<? extends ClassStructureBuilder<?>> methodBuilder =
                classBuilder.privateMethod(f.getType(), getPrivateFieldInjectorName(f));

        if (!f.isStatic()) {
          methodBuilder
                  .parameters(DefParameters.fromParameters(Parameter.of(f.getDeclaringClass(), "instance")));
        }

        methodBuilder.modifiers(modifiers)
                .body()
                ._(Stmt.try_()
                        ._(Stmt.nestedCall(Cast.to(f.getType(), Stmt.loadVariable(cachedField)
                                .invoke(getterName, f.isStatic() ? null : Refs.get("instance")))).returnValue())
                        .finish()
                        .catch_(Throwable.class, "e")
                        ._(Stmt.loadVariable("e").invoke("printStackTrace"))
                        ._(Stmt.throw_(RuntimeException.class, Refs.get("e")))
                        .finish())
                .finish();
      }
    }
  }

  /**
   * Generates methods for accessing a nonpublic constructor using either JSNI or
   * Java Reflection. The generated method will be private and static. The name
   * of the generated method can be discovered by calling
   * {@link #getPrivateMethodName(MetaMethod)}.
   *
   * @param useJSNIStubs
   *         If true, the generated method will use JSNI to access the field.
   *         Otherwise, Java reflection will be used (in this case, the
   *         generated code will not be GWT translatable).
   * @param classBuilder
   *         The class builder to add the generated method to.
   * @param m
   *         The constructor the generated method will invoke
   */
  public static void addPrivateAccessStubs(final boolean useJSNIStubs,
                                           final ClassStructureBuilder<?> classBuilder,
                                           final MetaConstructor m) {

    final DefParameters methodDefParms = DefParameters.from(m);

    if (useJSNIStubs) {
      classBuilder.publicMethod(m.getReturnType(), getPrivateMethodName(m))
              .parameters(methodDefParms)
              .modifiers(Modifier.Static, Modifier.JSNI)
              .body()
              ._(new StringStatement(JSNIUtil.methodAccess(m)))
              .finish();
    }
    else {
      final String cachedMethod = initCachedMethod(classBuilder, m);
      final Object[] args = new Object[methodDefParms.getParameters().size()];
      int i = 0;
      for (final Parameter p : methodDefParms.getParameters()) {
        args[i++] = Refs.get(p.getName());
      }

      final BlockBuilder<? extends ClassStructureBuilder> body = classBuilder.publicMethod(m.getReturnType(),
              getPrivateMethodName(m))
              .parameters(methodDefParms)
              .modifiers(Modifier.Static)
              .body();

      final Statement tryBuilder =
              Stmt.try_()
                      .append(
                              Stmt.nestedCall(
                                      Stmt.castTo(m.getReturnType(), Stmt.loadVariable(cachedMethod).invoke("newInstance",
                                              (Object) args))).returnValue())
                      .finish()
                      .catch_(Throwable.class, "e")
                      .append(Stmt.loadVariable("e").invoke("printStackTrace"))
                      .append(Stmt.throw_(RuntimeException.class, Refs.get("e")))
                      .finish();

      body.append(tryBuilder).finish();
    }
  }

  /**
   * Generates methods for accessing a nonpublic method using either JSNI or
   * Java Reflection. The generated method will be private and static. The name
   * of the generated method can be discovered by calling
   * {@link #getPrivateMethodName(MetaMethod)}.
   *
   * @param useJSNIStubs
   *         If true, the generated method will use JSNI to access the field.
   *         Otherwise, Java reflection will be used (in this case, the
   *         generated code will not be GWT translatable).
   * @param classBuilder
   *         The class builder to add the generated method to.
   * @param m
   *         The nonpublic method the generated method will invoke
   */
  public static void addPrivateAccessStubs(final boolean useJSNIStubs,
                                           final ClassStructureBuilder<?> classBuilder,
                                           final MetaMethod m) {
    addPrivateAccessStubs(useJSNIStubs, classBuilder, m, new Modifier[]{Modifier.Static});
  }

  /**
   * Generates methods for accessing a nonpublic method using either JSNI or Java
   * Reflection. The generated method will be private and static.
   *
   * @param useJSNIStubs
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
  public static void addPrivateAccessStubs(final boolean useJSNIStubs,
                                           final ClassStructureBuilder<?> classBuilder,
                                           final MetaMethod m,
                                           Modifier[] modifiers) {

    final List<Parameter> wrapperDefParms = new ArrayList<Parameter>();

    if (!m.isStatic()) {
      wrapperDefParms.add(Parameter.of(m.getDeclaringClass().getErased(), "instance"));
    }

    final List<Parameter> methodDefParms = DefParameters.from(m).getParameters();
    wrapperDefParms.addAll(methodDefParms);

    if (useJSNIStubs) {
      modifiers = appendJsni(modifiers);
      classBuilder.publicMethod(m.getReturnType(), getPrivateMethodName(m))
              .parameters(DefParameters.fromParameters(wrapperDefParms))
              .modifiers(modifiers)
              .body()
              ._(new StringStatement(JSNIUtil.methodAccess(m)))
              .finish();
    }
    else {
      final String cachedMethod = initCachedMethod(classBuilder, m);

      final Object[] args = new Object[methodDefParms.size()];

      int i = 0;
      for (final Parameter p : methodDefParms) {
        args[i++] = Refs.get(p.getName());
      }

      final BlockBuilder<? extends ClassStructureBuilder> body = classBuilder.publicMethod(m.getReturnType(),
              getPrivateMethodName(m))
              .parameters(DefParameters.fromParameters(wrapperDefParms))
              .modifiers(modifiers)
              .body();

      final BlockBuilder<CatchBlockBuilder> tryBuilder = Stmt.try_();

      final ContextualStatementBuilder statementBuilder = Stmt.loadVariable(cachedMethod)
              .invoke("invoke", m.isStatic() ? null : Refs.get("instance"), args);

      if (m.getReturnType().isVoid()) {
        tryBuilder._(statementBuilder);
      }
      else {
        tryBuilder._(statementBuilder.returnValue());
      }

      body._(tryBuilder
              .finish()
              .catch_(Throwable.class, "e")
              ._(Stmt.loadVariable("e").invoke("printStackTrace"))
              ._(Stmt.throw_(RuntimeException.class, Refs.get("e")))
              .finish())
              .finish();
    }
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
  private static Modifier[] appendJsni(Modifier[] modifiers) {
    final Modifier[] origModifiers = modifiers;
    modifiers = new Modifier[origModifiers.length + 1];
    System.arraycopy(origModifiers, 0, modifiers, 0, origModifiers.length);
    modifiers[modifiers.length - 1] = Modifier.JSNI;
    return modifiers;
  }

  private static String _getReflectionFieldMethGetName(final MetaField f) {
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

  private static String _getReflectionFieldMethSetName(final MetaField f) {
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
