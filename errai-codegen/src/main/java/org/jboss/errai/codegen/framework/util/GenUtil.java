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

package org.jboss.errai.codegen.framework.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.errai.codegen.framework.Cast;
import org.jboss.errai.codegen.framework.Context;
import org.jboss.errai.codegen.framework.DefModifiers;
import org.jboss.errai.codegen.framework.DefParameters;
import org.jboss.errai.codegen.framework.Modifier;
import org.jboss.errai.codegen.framework.Parameter;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.StringStatement;
import org.jboss.errai.codegen.framework.Variable;
import org.jboss.errai.codegen.framework.VariableReference;
import org.jboss.errai.codegen.framework.builder.BlockBuilder;
import org.jboss.errai.codegen.framework.builder.CatchBlockBuilder;
import org.jboss.errai.codegen.framework.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.framework.builder.ContextualStatementBuilder;
import org.jboss.errai.codegen.framework.builder.impl.Scope;
import org.jboss.errai.codegen.framework.exception.InvalidExpressionException;
import org.jboss.errai.codegen.framework.exception.InvalidTypeException;
import org.jboss.errai.codegen.framework.exception.OutOfScopeException;
import org.jboss.errai.codegen.framework.exception.TypeNotIterableException;
import org.jboss.errai.codegen.framework.exception.UndefinedMethodException;
import org.jboss.errai.codegen.framework.literal.LiteralFactory;
import org.jboss.errai.codegen.framework.literal.LiteralValue;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.meta.MetaClassMember;
import org.jboss.errai.codegen.framework.meta.MetaConstructor;
import org.jboss.errai.codegen.framework.meta.MetaField;
import org.jboss.errai.codegen.framework.meta.MetaMethod;
import org.jboss.errai.codegen.framework.meta.MetaParameter;
import org.jboss.errai.codegen.framework.meta.MetaParameterizedType;
import org.jboss.errai.codegen.framework.meta.MetaType;
import org.jboss.errai.codegen.framework.meta.MetaTypeVariable;
import org.mvel2.DataConversion;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class GenUtil {
  public static Statement[] generateCallParameters(Context context, Object... parameters) {
    Statement[] statements = new Statement[parameters.length];

    int i = 0;
    for (Object parameter : parameters) {
      statements[i++] = generate(context, parameter);
    }
    return statements;
  }

  public static Statement[] generateCallParameters(MetaMethod method, Context context, Object... parameters) {
    if (parameters.length != method.getParameters().length) {
      throw new UndefinedMethodException("Wrong number of parameters");
    }

    MetaParameter[] methParms = method.getParameters();

    Statement[] statements = new Statement[parameters.length];
    int i = 0;
    for (Object parameter : parameters) {
      if (parameter instanceof Statement) {
        if (((Statement) parameter).getType() == null) {
          parameter = generate(context, parameter);
        }
      }
      try {
        statements[i] = convert(context, parameter, methParms[i++].getType());
      }
      catch (InvalidTypeException e) {
        throw new RuntimeException("in method call: "
                + method.getDeclaringClass().getFullyQualifiedName()
                + "." + method.getName() + "(" + Arrays.toString(methParms) + ")", e);
      }
    }
    return statements;
  }

  public static Statement generate(Context context, Object o) {


    if (o instanceof VariableReference) {
      return context.getVariable(((VariableReference) o).getName());
    }
    else if (o instanceof Variable) {
      Variable v = (Variable) o;
      if (context.isScoped(v)) {
        return v.getReference();
      }
      else {
        throw new OutOfScopeException("variable cannot be referenced from this scope: " + v.getName());
      }
    }
    else if (o instanceof Statement) {
      ((Statement) o).generate(context);
      return (Statement) o;
    }
    else {
      return LiteralFactory.getLiteral(o);
    }
  }

  public static void assertIsIterable(Statement statement) {
    Class<?> cls = statement.getType().asClass();

    if (!cls.isArray() && !Iterable.class.isAssignableFrom(cls))
      throw new TypeNotIterableException(statement.generate(Context.create()));
  }

  public static void assertAssignableTypes(MetaClass from, MetaClass to) {
    if (!to.asBoxed().isAssignableFrom(from.asBoxed())) {
      throw new InvalidTypeException(to.getFullyQualifiedName() + " is not assignable from "
              + from.getFullyQualifiedName());
    }
  }

  public static Statement convert(Context context, Object input, MetaClass targetType) {
    try {
      if (input instanceof Statement) {
        if (input instanceof LiteralValue<?>) {
          input = ((LiteralValue<?>) input).getValue();
        }
        else {
          if ("null".equals(((Statement) input).generate(context))) {
            return (Statement) input;
          }

          assertAssignableTypes(((Statement) input).getType(), targetType);
          return (Statement) input;
        }
      }

      if (Object.class.getName().equals(targetType.getFullyQualifiedName())) {
        return generate(context, input);
      }

      Class<?> inputClass = input == null ? Object.class : input.getClass();
      Class<?> targetClass = targetType.asBoxed().asClass();
      if (DataConversion.canConvert(targetClass, inputClass)) {
        return generate(context, DataConversion.convert(input, targetClass));
      }
      else {
        return generate(context, input);
      }
    }
    catch (OutOfScopeException e) {
      throw e;
    }
    catch (Throwable t) {
      throw new InvalidTypeException(t);
    }
  }

  public static String classesAsStrings(MetaClass... stmt) {
    StringBuilder buf = new StringBuilder(128);
    for (int i = 0; i < stmt.length; i++) {
      buf.append(stmt[i].getFullyQualifiedName());
      if (i + 1 < stmt.length) {
        buf.append(", ");
      }
    }
    return buf.toString();
  }

  public static MetaClass[] fromParameters(MetaParameter... parms) {
    List<MetaClass> parameters = new ArrayList<MetaClass>();
    for (MetaParameter metaParameter : parms) {
      parameters.add(metaParameter.getType());
    }
    return parameters.toArray(new MetaClass[parameters.size()]);
  }

  public static MetaClass[] classToMeta(Class<?>[] types) {
    MetaClass[] metaClasses = new MetaClass[types.length];
    for (int i = 0; i < types.length; i++) {
      metaClasses[i] = MetaClassFactory.get(types[i]);
    }
    return metaClasses;
  }


  public static Map<String, MetaClass> determineTypeVariables(MetaMethod method) {
    HashMap<String, MetaClass> typeVariables = new HashMap<String, MetaClass>();

    int methodParmIndex = 0;
    for (MetaType methodParmType : method.getGenericParameterTypes()) {
      MetaParameter parm = method.getParameters()[methodParmIndex];
      resolveTypeVariable(typeVariables, methodParmType, parm.getType());
      methodParmIndex++;
    }

    return typeVariables;
  }


  private static void resolveTypeVariable(Map<String, MetaClass> typeVariables,
                                          MetaType methodParmType, MetaType callParmType) {
    if (methodParmType instanceof MetaTypeVariable) {
      MetaTypeVariable typeVar = (MetaTypeVariable) methodParmType;
      typeVariables.put(typeVar.getName(), (MetaClass) callParmType);
    }
    else if (methodParmType instanceof MetaParameterizedType) {
      MetaType parameterizedCallParmType;
      if (callParmType instanceof MetaParameterizedType) {
        parameterizedCallParmType = callParmType;
      }
      else {
        parameterizedCallParmType = ((MetaClass) callParmType).getParameterizedType();
      }

      MetaParameterizedType parameterizedMethodParmType = (MetaParameterizedType) methodParmType;
      int typeParmIndex = 0;
      for (MetaType typeParm : parameterizedMethodParmType.getTypeParameters()) {
        if (parameterizedCallParmType != null) {
          resolveTypeVariable(typeVariables, typeParm,
                  ((MetaParameterizedType) parameterizedCallParmType).getTypeParameters()[typeParmIndex++]);
        }
        else {
          resolveTypeVariable(typeVariables, typeParm, callParmType);
        }
      }
    }
  }

  public static Scope scopeOf(MetaClass clazz) {
    if (clazz.isPublic()) {
      return Scope.Public;
    }
    else if (clazz.isPrivate()) {
      return Scope.Private;
    }
    else if (clazz.isProtected()) {
      return Scope.Protected;
    }
    else {
      return Scope.Package;
    }
  }

  public static Scope scopeOf(MetaClassMember member) {
    if (member.isPublic()) {
      return Scope.Public;
    }
    else if (member.isPrivate()) {
      return Scope.Private;
    }
    else if (member.isProtected()) {
      return Scope.Protected;
    }
    else {
      return Scope.Package;
    }
  }

  public static DefModifiers modifiersOf(MetaClassMember member) {
    DefModifiers defModifiers = new DefModifiers();
    if (member.isAbstract()) {
      defModifiers.addModifiers(Modifier.Abstract);
    }
    else if (member.isFinal()) {
      defModifiers.addModifiers(Modifier.Final);
    }
    else if (member.isStatic()) {
      defModifiers.addModifiers(Modifier.Static);
    }
    else if (member.isSynchronized()) {
      defModifiers.addModifiers(Modifier.Synchronized);
    }
    else if (member.isVolatile()) {
      defModifiers.addModifiers(Modifier.Volatile);
    }
    else if (member.isTransient()) {
      defModifiers.addModifiers(Modifier.Transient);
    }
    return defModifiers;
  }

  public static boolean equals(MetaField a, MetaField b) {
    return a.getName().equals(b.getName()) && !a.getType().equals(b.getType())
            && !a.getDeclaringClass().equals(b.getDeclaringClass());
  }

  public static boolean equals(MetaConstructor a, MetaConstructor b) {
//    if (!a.getName().equals(b.getName())) return false;
    if (a.getParameters().length != b.getParameters().length) {
      return false;
    }

    for (int i = 0; i < a.getParameters().length; i++) {
      if (!equals(a.getParameters()[i], b.getParameters()[i])) {
        return false;
      }
    }

    if (!a.getDeclaringClass().equals(b.getDeclaringClass())) {
      return false;
    }

    return true;
  }

  public static boolean equals(MetaMethod a, MetaMethod b) {
    if (!a.getName().equals(b.getName())) return false;
    if (a.getParameters().length != b.getParameters().length) return false;
    if (!a.getDeclaringClass().equals(b.getDeclaringClass())) return false;

    for (int i = 0; i < a.getParameters().length; i++) {
      if (!equals(a.getParameters()[i], b.getParameters()[i])) return false;
    }
    return true;
  }


  public static boolean equals(MetaParameter a, MetaParameter b) {
    return a.getType().isAssignableFrom(b.getType()) || b.getType().isAssignableFrom(a.getType());
  }


  private static final String JAVA_REFL_FLD_UTIL_METH = "_getAccessibleField";

  public static void createJavaReflectionFieldInitializerUtilMethod(ClassStructureBuilder<?> classBuilder) {

    if (classBuilder.getClassDefinition().getMethod(JAVA_REFL_FLD_UTIL_METH, Class.class, Field.class) != null) {
      return;
    }

    classBuilder.privateMethod(Field.class, "_getAccessibleField").modifiers(Modifier.Static)
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

  private static final String JAVA_REFL_METH_UTIL_METH = "_getAccessibleMethod";

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


  public static void addPrivateAccessStubs(boolean useJSNIStubs, ClassStructureBuilder<?> classBuilder, MetaField f) {
    MetaClass type = f.getType();
    if (type.getCanonicalName().equals("long")) {
      type = type.asBoxed();
    }

    if (useJSNIStubs) {
      classBuilder.privateMethod(void.class, getPrivateFieldInjectorName(f))
              .parameters(DefParameters.fromParameters(Parameter.of(f.getDeclaringClass(), "instance"),
                      Parameter.of(type.isArray() ? type.asBoxed() : type, "value")))
              .modifiers(Modifier.Static, Modifier.JSNI)
              .body()
              .append(new StringStatement(JSNIUtil.fieldAccess(f) + " = value"))
              .finish();

      classBuilder.privateMethod(type, getPrivateFieldInjectorName(f))
              .parameters(DefParameters.fromParameters(Parameter.of(f.getDeclaringClass(), "instance")))
              .modifiers(Modifier.Static, Modifier.JSNI)
              .body()
              .append(new StringStatement("return " + JSNIUtil.fieldAccess(f)))
              .finish();
    }
    else {
      /**
       * Reflection stubs
       */

      String cachedField = initCachedField(classBuilder, f);
      String setterName = _getReflectionFieldMethSetName(f);

      classBuilder.privateMethod(void.class, getPrivateFieldInjectorName(f))
              .parameters(DefParameters.fromParameters(Parameter.of(f.getDeclaringClass(), "instance"),
                      Parameter.of(f.getType().isArray() ? f.getType().asBoxed() : f.getType(), "value")))
              .modifiers(Modifier.Static)
              .body()
              .append(Stmt.try_()
                      .append(Stmt.loadVariable(cachedField).invoke(setterName, Refs.get("instance"), Refs.get("value")))
                      .finish()
                      .catch_(Throwable.class, "e")
                      .append(Stmt.loadVariable("e").invoke("printStackTrace"))
                      .append(Stmt.throw_(RuntimeException.class, Refs.get("e")))
                      .finish())
              .finish();

      String getterName = _getReflectionFieldMethGetName(f);

      classBuilder.privateMethod(f.getType(), getPrivateFieldInjectorName(f))
              .parameters(DefParameters.fromParameters(Parameter.of(f.getDeclaringClass(), "instance")))
              .modifiers(Modifier.Static)
              .body()
              .append(Stmt.try_()
                      .append(Stmt.nestedCall(Cast.to(f.getType(), Stmt.loadVariable(cachedField)
                              .invoke(getterName, Refs.get("instance")))).returnValue())
                      .finish()
                      .catch_(Throwable.class, "e")
                      .append(Stmt.loadVariable("e").invoke("printStackTrace"))
                      .append(Stmt.throw_(RuntimeException.class, Refs.get("e")))
                      .finish())
              .finish();
    }
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


  public static void addPrivateAccessStubs(boolean useJSNIStubs, ClassStructureBuilder<?> classBuilder, MetaMethod m) {
    List<Parameter> wrapperDefParms = new ArrayList<Parameter>();
    wrapperDefParms.add(Parameter.of(m.getDeclaringClass(), "instance"));
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
              .invoke("invoke", Refs.get("instance"), args);

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

  public static MetaClass getPrimitiveWrapper(MetaClass clazz) {
    if (clazz.isPrimitive()) {
      if ("int".equals(clazz.getCanonicalName())) {
        return MetaClassFactory.get(Integer.class);
      }
      else if ("boolean".equals(clazz.getCanonicalName())) {
        return MetaClassFactory.get(Boolean.class);
      }
      else if ("long".equals(clazz.getCanonicalName())) {
        return MetaClassFactory.get(Long.class);
      }
      else if ("double".equals(clazz.getCanonicalName())) {
        return MetaClassFactory.get(Double.class);
      }
      else if ("float".equals(clazz.getCanonicalName())) {
        return MetaClassFactory.get(Float.class);
      }
      else if ("short".equals(clazz.getCanonicalName())) {
        return MetaClassFactory.get(Short.class);
      }
      else if ("char".equals(clazz.getCanonicalName())) {
        return MetaClassFactory.get(Character.class);
      }
      else if ("byte".equals(clazz.getCanonicalName())) {
        return MetaClassFactory.get(Byte.class);
      }
    }
    return clazz;
  }

  public static boolean isPrimitiveWrapper(MetaClass clazz) {
    return Integer.class.getName().equals(clazz.getFullyQualifiedName())
            || Boolean.class.getName().equals(clazz.getFullyQualifiedName())
            || Long.class.getName().equals(clazz.getFullyQualifiedName())
            || Double.class.getName().equals(clazz.getFullyQualifiedName())
            || Float.class.getName().equals(clazz.getFullyQualifiedName())
            || Short.class.getName().equals(clazz.getFullyQualifiedName())
            || Character.class.getName().equals(clazz.getFullyQualifiedName())
            || Byte.class.getName().equals(clazz.getFullyQualifiedName());
  }

  public static int getArrayDimensions(MetaClass type) {
    if (!type.isArray()) return 0;

    String internalName = type.getInternalName();
    for (int i = 0; i < internalName.length(); i++) {
      if (internalName.charAt(i) != '[') return i;
    }
    return 0;
  }

  public static MetaMethod findCaseInsensitiveMatch(MetaClass retType, MetaClass clazz, String name, MetaClass... parms) {
    MetaClass c = clazz;

    do {
      Outer:
      for (MetaMethod method : c.getDeclaredMethods()) {
        if (name.equalsIgnoreCase(method.getName())) {
          if (parms.length != method.getParameters().length) continue;

          MetaParameter[] mps = method.getParameters();
          for (int i = 0; i < parms.length; i++) {
            if (!parms[i].getFullyQualifiedName().equals(mps[i].getType().getFullyQualifiedName())) {
              continue Outer;
            }
          }

          if (retType != null
                  && !retType.getFullyQualifiedName().equals(method.getReturnType().getFullyQualifiedName())) {
            continue;
          }

          return method;
        }
      }
    }
    while ((c = c.getSuperClass()) != null);

    return null;
  }

  public static void throwIfUnhandled(String error, Throwable t) {
    try {
      throw t;
    }
    catch (OutOfScopeException e) {
      throw e;
    }
    catch (InvalidExpressionException e) {
      throw e;
    }
    catch (InvalidTypeException e) {
      throw e;
    }
    catch (UndefinedMethodException e) {
      throw e;
    }
    catch (TypeNotIterableException e) {
      throw e;
    }
    catch (Throwable e) {
      throw new RuntimeException("generation failure at: " + error, e);
    }
  }
}