package org.jboss.errai.codegen.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.codegen.Cast;
import org.jboss.errai.codegen.DefParameters;
import org.jboss.errai.codegen.Modifier;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.StringStatement;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.CatchBlockBuilder;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.ContextualStatementBuilder;
import org.jboss.errai.codegen.builder.MethodBlockBuilder;
import org.jboss.errai.codegen.builder.MethodCommentBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;

/**
 * @author Mike Brock
 */
public class PrivateAccessUtil {


  private static final String JAVA_REFL_FLD_UTIL_METH = "_getAccessibleField";

  private static final String JAVA_REFL_METH_UTIL_METH = "_getAccessibleMethod";


  public static void createJavaReflectionFieldInitializerUtilMethod(ClassStructureBuilder<?> classBuilder) {

    if (classBuilder.getClassDefinition().getMethod(JAVA_REFL_FLD_UTIL_METH, Class.class, Field.class) != null) {
      return;
    }

    classBuilder.privateMethod(Field.class, JAVA_REFL_FLD_UTIL_METH).modifiers(Modifier.Static)
            .parameters(DefParameters.of(Parameter.of(Class.class, "cls"), Parameter.of(String.class, "name")))
            .body()
            .append(Stmt.try_()
                    .append(Stmt.declareVariable("fld", Stmt.loadVariable("cls").invoke("getDeclaredField",
                            Stmt.loadVariable("name"))))
                    .append(Stmt.loadVariable("fld").invoke("setAccessible", true))
                    .append(Stmt.loadVariable("fld").returnValue())
                    .finish()
                    .catch_(Throwable.class, "e")
                    .append(Stmt.loadVariable("e").invoke("printStackTrace"))
                    .append(Stmt.throw_(RuntimeException.class, Refs.get("e")))
                    .finish())
            .finish();
  }

  public static void createJavaReflectionMethodInitializerUtilMethod(
          ClassStructureBuilder<?> classBuilder) {

    if (classBuilder.getClassDefinition().getMethod(JAVA_REFL_METH_UTIL_METH, Class.class, String.class, Class[].class) != null) {
      return;
    }

    classBuilder.privateMethod(Method.class, JAVA_REFL_METH_UTIL_METH).modifiers(Modifier.Static)
            .parameters(DefParameters.of(Parameter.of(Class.class, "cls"), Parameter.of(String.class, "name"),
                    Parameter.of(Class[].class, "parms")))
            .body()
            .append(Stmt.try_()
                    .append(Stmt.declareVariable("meth", Stmt.loadVariable("cls").invoke("getDeclaredMethod",
                            Stmt.loadVariable("name"), Stmt.loadVariable("parms"))))
                    .append(Stmt.loadVariable("meth").invoke("setAccessible", true))
                    .append(Stmt.loadVariable("meth").returnValue())
                    .finish()
                    .catch_(Throwable.class, "e")
                    .append(Stmt.loadVariable("e").invoke("printStackTrace"))
                    .append(Stmt.throw_(RuntimeException.class, Refs.get("e")))
                    .finish())
            .finish();
  }

  public static String initCachedField(ClassStructureBuilder<?> classBuilder, MetaField f) {
    createJavaReflectionFieldInitializerUtilMethod(classBuilder);

    String fieldName = getPrivateFieldInjectorName(f) + "_fld";

    classBuilder.privateField(fieldName, Field.class).modifiers(Modifier.Static)
            .initializesWith(Stmt.invokeStatic(classBuilder.getClassDefinition(), JAVA_REFL_FLD_UTIL_METH,
                    f.getDeclaringClass(), f.getName())).finish();

    return fieldName;
  }

  public static String initCachedMethod(ClassStructureBuilder<?> classBuilder, MetaMethod m) {
    createJavaReflectionMethodInitializerUtilMethod(classBuilder);

    String fieldName = getPrivateMethodName(m) + "_meth";

    classBuilder.privateField(fieldName, Method.class).modifiers(Modifier.Static)
            .initializesWith(Stmt.invokeStatic(classBuilder.getClassDefinition(), JAVA_REFL_METH_UTIL_METH,
                    m.getDeclaringClass(), m.getName(), MetaClassFactory.asClassArray(m.getParameters()))).finish();

    return fieldName;
  }

  /**
   * Generates methods for accessing a private field using either JSNI or Java
   * Reflection. The generated methods will be private and static.
   *
   * @param useJSNIStubs
   *          If true, the generated methods will use JSNI to access the field.
   *          Otherwise, Java reflection will be used (in this case, the
   *          generated code will not be GWT translatable).
   * @param classBuilder
   *          The class builder to add the generated methods to.
   * @param f
   *          The field the generated accessors read and write.
   */
  public static void addPrivateAccessStubs(boolean useJSNIStubs,
                                           ClassStructureBuilder<?> classBuilder,
                                           MetaField f) {
    addPrivateAccessStubs(PrivateAccessType.Both, useJSNIStubs, classBuilder, f);
  }

  /**
   * Generates methods for accessing a private field using either JSNI or Java
   * Reflection. The generated methods will be private and static.
   *
   * @param accessType
   *          Whether to generate a read method, a write method, or both.
   * @param useJSNIStubs
   *          If true, the generated methods will use JSNI to access the field.
   *          Otherwise, Java reflection will be used (in this case, the
   *          generated code will not be GWT translatable).
   * @param classBuilder
   *          The class builder to add the generated methods to.
   * @param f
   *          The field the generated accessors read and write.
   */
  public static void addPrivateAccessStubs(PrivateAccessType accessType,
          boolean useJSNIStubs,
          ClassStructureBuilder<?> classBuilder,
          MetaField f) {
    addPrivateAccessStubs(accessType, useJSNIStubs, classBuilder, f, new Modifier[] { Modifier.Static });
  }

  /**
   * Generates methods for accessing a private field using either JSNI or Java
   * Reflection. The generated methods will be private.
   *
   * @param accessType
   *          Whether to generate a read method, a write method, or both.
   * @param useJSNIStubs
   *          If true, the generated methods will use JSNI to access the field.
   *          Otherwise, Java reflection will be used (in this case, the
   *          generated code will not be GWT translatable).
   * @param classBuilder
   *          The class builder to add the generated methods to.
   * @param f
   *          The field the generated accessors read and write.
   * @param modifiers
   *          The modifiers on the generated method, for example
   *          {@link Modifier#Final} or {@link Modifier#Synchronized}. <i>Never
   *          specify {@code Modifier.JSNI}</i>; it is added automatically when
   *          needed.
   */
  public static void addPrivateAccessStubs(PrivateAccessType accessType,
                                           boolean useJSNIStubs,
                                           ClassStructureBuilder<?> classBuilder,
                                           MetaField f,
                                           Modifier[] modifiers) {
    MetaClass type = f.getType();
    if (type.getCanonicalName().equals("long")) {
      type = type.asBoxed();
    }

    boolean read = accessType == PrivateAccessType.Read || accessType == PrivateAccessType.Both;
    boolean write = accessType == PrivateAccessType.Write || accessType == PrivateAccessType.Both;

    if (useJSNIStubs) {

      // append JSNI modifier to the given modifier array
      Modifier[] origModifiers = modifiers;
      modifiers = new Modifier[origModifiers.length + 1];
      System.arraycopy(origModifiers, 0, modifiers, 0, origModifiers.length);
      modifiers[modifiers.length - 1] = Modifier.JSNI;

      if (write) {
        final MethodCommentBuilder<? extends ClassStructureBuilder<?>> methodBuilder
                = classBuilder.privateMethod(void.class, getPrivateFieldInjectorName(f));

        if (!f.isStatic()) {
          methodBuilder
                  .parameters(DefParameters.fromParameters(Parameter.of(f.getDeclaringClass(), "instance"),
                          Parameter.of(type, "value")));
        }

        methodBuilder.modifiers(modifiers)
                .body()
                .append(new StringStatement(JSNIUtil.fieldAccess(f) + " = value"))
                .finish();
      }

      if (read) {
        MethodBlockBuilder<? extends ClassStructureBuilder<?>> instance
                = classBuilder.privateMethod(type, getPrivateFieldInjectorName(f));

        if (!f.isStatic()) {
          instance.parameters(DefParameters.fromParameters(Parameter.of(f.getDeclaringClass(), "instance")));
        }

        instance.modifiers(modifiers)
                .body()
                .append(new StringStatement("return " + JSNIUtil.fieldAccess(f)))
                .finish();
      }
    }
    else {

      /*
       * Reflection stubs
       */

      String cachedField = initCachedField(classBuilder, f);
      String setterName = _getReflectionFieldMethSetName(f);

      if (write) {
        MethodCommentBuilder<? extends ClassStructureBuilder<?>> methodBuilder
                = classBuilder.privateMethod(void.class, getPrivateFieldInjectorName(f));

        if (!f.isStatic()) {
          methodBuilder
                  .parameters(DefParameters.fromParameters(Parameter.of(f.getDeclaringClass(), "instance"),
                          Parameter.of(f.getType(), "value")));
        }

        methodBuilder.modifiers(modifiers)
                .body()
                .append(Stmt.try_()
                        .append(Stmt.loadVariable(cachedField).invoke(setterName, Refs.get("instance"), Refs.get("value")))
                        .finish()
                        .catch_(Throwable.class, "e")
                        .append(Stmt.loadVariable("e").invoke("printStackTrace"))
                        .append(Stmt.throw_(RuntimeException.class, Refs.get("e")))
                        .finish())
                .finish();
      }

      String getterName = _getReflectionFieldMethGetName(f);

      if (read) {
        MethodCommentBuilder<? extends ClassStructureBuilder<?>> methodBuilder
                = classBuilder.privateMethod(f.getType(), getPrivateFieldInjectorName(f));


        if (!f.isStatic()) {
          methodBuilder
                  .parameters(DefParameters.fromParameters(Parameter.of(f.getDeclaringClass(), "instance")));
        }

        methodBuilder.modifiers(modifiers)
                .body()
                .append(Stmt.try_()
                        .append(Stmt.nestedCall(Cast.to(f.getType(), Stmt.loadVariable(cachedField)
                                .invoke(getterName, f.isStatic() ? null : Refs.get("instance")))).returnValue())
                        .finish()
                        .catch_(Throwable.class, "e")
                        .append(Stmt.loadVariable("e").invoke("printStackTrace"))
                        .append(Stmt.throw_(RuntimeException.class, Refs.get("e")))
                        .finish())
                .finish();
      }
    }
  }

  public static void addPrivateAccessStubs(boolean useJSNIStubs, ClassStructureBuilder<?> classBuilder, MetaMethod m) {
    List<Parameter> wrapperDefParms = new ArrayList<Parameter>();

    if (!m.isStatic()) {
      wrapperDefParms.add(Parameter.of(m.getDeclaringClass().getErased(), "instance"));
    }

    List<Parameter> methodDefParms = DefParameters.from(m).getParameters();

    wrapperDefParms.addAll(methodDefParms);

    if (useJSNIStubs) {
      classBuilder.publicMethod(m.getReturnType(), getPrivateMethodName(m))
              .parameters(new DefParameters(wrapperDefParms))
              .modifiers(Modifier.Static, Modifier.JSNI)
              .body()
              .append(new StringStatement(JSNIUtil.methodAccess(m)))
              .finish();
    }
    else {
      String cachedMethod = initCachedMethod(classBuilder, m);

      Object[] args = new Object[methodDefParms.size()];

      int i = 0;
      for (Parameter p : methodDefParms) {
        args[i++] = Refs.get(p.getName());
      }

      BlockBuilder<? extends ClassStructureBuilder> body = classBuilder.publicMethod(m.getReturnType(),
              getPrivateMethodName(m))
              .parameters(new DefParameters(wrapperDefParms))
              .modifiers(Modifier.Static)
              .body();

      BlockBuilder<CatchBlockBuilder> tryBuilder = Stmt.try_();

      ContextualStatementBuilder statementBuilder = Stmt.loadVariable(cachedMethod)
              .invoke("invoke", m.isStatic() ? null : Refs.get("instance"), args);

      if (m.getReturnType().isVoid()) {
        tryBuilder.append(statementBuilder);
      }
      else {
        tryBuilder.append(statementBuilder.returnValue());
      }

      body.append(tryBuilder
              .finish()
              .catch_(Throwable.class, "e")
              .append(Stmt.loadVariable("e").invoke("printStackTrace"))
              .append(Stmt.throw_(RuntimeException.class, Refs.get("e")))
              .finish())
              .finish();
    }
  }

  public static String getPrivateFieldInjectorName(MetaField field) {
    return field.getDeclaringClass()
            .getFullyQualifiedName().replaceAll("\\.", "_") + "_" + field.getName();
  }

  public static String getPrivateMethodName(MetaMethod method) {
    StringBuilder buf = new StringBuilder(method.getDeclaringClass()
            .getFullyQualifiedName().replaceAll("\\.", "_") + "_" + method.getName());

    for (MetaParameter parm : method.getParameters()) {
      buf.append('_').append(parm.getType().getFullyQualifiedName().replaceAll("\\.", "_"));
    }

    return buf.toString();
  }

  private static String _getReflectionFieldMethGetName(MetaField f) {
    MetaClass t = f.getType();

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

  private static String _getReflectionFieldMethSetName(MetaField f) {
    MetaClass t = f.getType();

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
