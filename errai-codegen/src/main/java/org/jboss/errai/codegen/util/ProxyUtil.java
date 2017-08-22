/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.codegen.util;

import com.google.common.reflect.TypeToken;
import org.jboss.errai.codegen.BlockStatement;
import org.jboss.errai.codegen.BooleanOperator;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.StringStatement;
import org.jboss.errai.codegen.Variable;
import org.jboss.errai.codegen.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.builder.ElseBlockBuilder;
import org.jboss.errai.codegen.builder.impl.BooleanExpressionBuilder;
import org.jboss.errai.codegen.meta.MetaAnnotation;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.api.interceptor.RemoteCallContext;
import org.jboss.errai.common.client.util.AsyncBeanFactory;
import org.jboss.errai.common.client.util.CreationalCallback;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.inject.Singleton;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

/**
 * Utilities to avoid redundant code for proxy generation.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Mike Brock
 */
public abstract class ProxyUtil {

  private ProxyUtil() {
  }

  /**
   * Generates the {@link org.jboss.errai.common.client.api.interceptor.CallContext} for method
   * interception. Ignores annotations in non-translatable packages.
   *
   * @param callContextType the type of {@link org.jboss.errai.common.client.api.interceptor.RemoteCallContext} to
   *                        use.
   * @param proxyClass      the declaring proxy class
   * @param method          the method that is being proxied.
   * @param proceed         the logic that should be invoked if
   *                        {@link org.jboss.errai.common.client.api.interceptor.CallContext#proceed()} is called.
   * @param interceptors    a list of interceptors to use
   * @return statement representing an anonymous implementation of the provided
   * {@link org.jboss.errai.common.client.api.interceptor.CallContext}
   */
  public static AnonymousClassStructureBuilder generateProxyMethodCallContext(final Class<? extends RemoteCallContext> callContextType,
          final MetaClass proxyClass,
          final MetaMethod method,
          final Statement proceed,
          final List<MetaClass> interceptors,
          final AnnotationFilter annotationFilter,
          final boolean iocEnabled) {

    final MetaAnnotation[] methodFilteredAnnotations = annotationFilter.filter(method.getAnnotations())
            .toArray(new MetaAnnotation[0]);

    final MetaAnnotation[] methodsDeclaringClassAnnotations = annotationFilter.filter(method.getDeclaringClass().getAnnotations())
            .toArray(new MetaAnnotation[0]);

    return Stmt.newObject(callContextType)
            .extend()
            .publicOverridesMethod("getMethodName")
            .append(Stmt.load(method.getName()).returnValue())
            .finish()
            .publicOverridesMethod("getReturnType")
            .append(Stmt.load(method.getReturnType()).returnValue())
            .finish()
            .publicOverridesMethod("getAnnotations")
            .append(Stmt.load(methodFilteredAnnotations).returnValue())
            .finish()
            .publicOverridesMethod("getTypeAnnotations")
            .append(Stmt.load(methodsDeclaringClassAnnotations).returnValue())
            .finish()
            .publicOverridesMethod("proceed")
            .append(generateInterceptorStackProceedMethod(callContextType, proceed, interceptors, iocEnabled))
            .append(Stmt.load(null).returnValue())
            .finish()
            .publicOverridesMethod("proceed", Parameter.of(RemoteCallback.class, "interceptorCallback", true))
            .append(Stmt.declareVariable(RemoteCallback.class)
                    .asFinal()
                    .named("providedCallback")
                    .initializeWith(Stmt.loadStatic(proxyClass, "this").loadField("remoteCallback")))
            .append(Stmt.loadVariable("remoteCallback")
                    .assignValue(Stmt.newObject(RemoteCallback.class)
                            .extend()
                            .publicOverridesMethod("callback", Parameter.of(Object.class, "response"))
                            .append(Stmt.declareVariable(RemoteCallback.class)
                                    .named("intCallback")
                                    .initializeWith(Stmt.loadVariable("interceptorCallback")))
                            .append(StringStatement.of("setResult(response)"))
                            .append(Stmt.loadVariable("intCallback")
                                    .invoke("callback", StringStatement.of("getResult()", Object.class)))
                            .append(Stmt.loadVariable("providedCallback")
                                    .invoke("callback", StringStatement.of("getResult()", Object.class)))
                            .finish()
                            .finish()))
            .append(Stmt.loadVariable("this").invoke("proceed"))
            .finish()
            .publicOverridesMethod("proceed", Parameter.of(RemoteCallback.class, "interceptorCallback"),
                    Parameter.of(ErrorCallback.class, "interceptorErrorCallback", true))
            .append(Stmt.declareVariable(ErrorCallback.class)
                    .asFinal()
                    .named("providedErrorCallback")
                    .initializeWith(Stmt.loadStatic(proxyClass, "this").loadField("errorCallback")))
            .append(Stmt.loadVariable("errorCallback")
                    .assignValue(Stmt.newObject(ErrorCallback.class)
                            .extend()
                            .publicOverridesMethod("error", Parameter.of(Object.class, "message"),
                                    Parameter.of(Throwable.class, "throwable"))
                            .append(Stmt.loadVariable("interceptorErrorCallback")
                                    .invoke("error", Variable.get("message"), Variable.get("throwable")))
                            .append(Stmt.if_(
                                    BooleanExpressionBuilder.create(StringStatement.of("getResult()", Object.class),
                                            BooleanOperator.NotEquals, Stmt.loadLiteral(null)))
                                    .append(Stmt.loadVariable("remoteCallback")
                                            .invoke("callback", StringStatement.of("getResult()", Object.class)))
                                    .append(Stmt.load(false).returnValue())
                                    .finish()
                                    .elseif_(BooleanExpressionBuilder.create(Stmt.loadVariable("providedErrorCallback"),
                                            BooleanOperator.NotEquals, Stmt.loadLiteral(null)))
                                    .append(Stmt.nestedCall(Stmt.loadVariable("providedErrorCallback")
                                            .invoke("error", Variable.get("message"), Variable.get("throwable")))
                                            .returnValue())
                                    .finish())
                            .append(Stmt.load(true).returnValue())
                            .finish()
                            .finish()))
            .append(Stmt.loadVariable("this").invoke("proceed", Variable.get("interceptorCallback")))
            .finish();
  }

  private static Statement generateInterceptorStackProceedMethod(final Class<? extends RemoteCallContext> callContextType,
          final Statement proceed,
          final List<MetaClass> interceptors,
          final boolean iocEnabled) {

    final BlockStatement proceedLogic = new BlockStatement();
    proceedLogic.addStatement(Stmt.loadVariable("status").invoke("proceed"));

    ElseBlockBuilder interceptorStack = If.isNull(Stmt.loadVariable("status").invoke("getNextInterceptor"))
            .append(proceed)
            .finish();

    for (final MetaClass interceptor : interceptors) {
      interceptorStack = interceptorStack.elseif_(
              Bool.equals(Stmt.loadVariable("status").invoke("getNextInterceptor"), Stmt.load(interceptor)))
              .append(Stmt.declareFinalVariable("ctx", callContextType, Stmt.loadVariable("this")))
              .append(Stmt.declareVariable(CreationalCallback.class)
                      .asFinal()
                      .named("icc")
                      .initializeWith(Stmt.newObject(CreationalCallback.class)
                              .extend()
                              .publicOverridesMethod("callback", Parameter.of(Object.class, "beanInstance", true))
                              .append(Stmt.loadVariable("status").invoke("setProceeding", false))
                              .append(Stmt.castTo(interceptor, Stmt.loadVariable("beanInstance"))
                                      .invoke("aroundInvoke", Variable.get("ctx")))
                              .append(If.cond(Bool.and(Bool.notExpr(Stmt.loadVariable("status").invoke("isProceeding")),
                                      Bool.equals(Stmt.loadLiteral(interceptor),
                                              Stmt.loadVariable("status").invoke("getNextInterceptor"))))
                                      .append(Stmt.loadVariable("remoteCallback")
                                              .invoke("callback", Stmt.loadVariable("ctx").invoke("getResult")))
                                      .finish())
                              .finish() // finish the method override body
                              .finish() // finish the anonymous CreationalCallback class body
                      ))
              .append(generateAsyncInterceptorCreation(interceptor, iocEnabled))
              .finish();
    }
    proceedLogic.addStatement(interceptorStack.else_().finish());
    return proceedLogic;
  }

  /**
   * Generates the code that will create the interceptor and then invoke the callback when done. If
   * IOC is available *and* the interceptor is a managed bean, then the IOC bean manager will be
   * used to load the interceptor.
   *
   * @param iocEnabled
   * @param interceptor
   */
  private static Statement generateAsyncInterceptorCreation(final MetaClass interceptor, final boolean iocEnabled) {
    if (iocEnabled && isManagedBean(interceptor)) {
      // Note: for the IOC path, generate the code via StringStatement because we
      // need to make sure that IOC is an optional dependency. This should probably
      // be replaced with some sort of pluggable model instead (where a Statement can
      // be provided by some Provider in the IOC module itself maybe).
      final StringBuilder builder = new StringBuilder();
      builder.append("org.jboss.errai.ioc.client.container.IOC.getAsyncBeanManager().lookupBeans(")
              .append(interceptor.getName())
              .append(".class).iterator().next().getInstance(icc)");
      return new StringStatement(builder.toString());
    } else {
      return Stmt.invokeStatic(AsyncBeanFactory.class, "createBean", Stmt.newObject(interceptor), Variable.get("icc"));
    }
  }

  /**
   * Returns true if the given bean is an explicitly managed bean (meaning it is annotated in some
   * way).
   *
   * @param interceptor
   */
  private static boolean isManagedBean(final MetaClass interceptor) {
    return interceptor.getAnnotation(ApplicationScoped.class) != null
            || interceptor.getAnnotation(Singleton.class) != null || interceptor.getAnnotation(Dependent.class) != null;
  }

  public static boolean shouldProxyMethod(final MetaMethod method) {
    final String methodName = method.getName();

    return !method.isFinal() && !method.isStatic() && !method.isPrivate() && !methodName.equals("hashCode")
            && !methodName.equals("equals") && !methodName.equals("toString") && !methodName.equals("clone")
            && !methodName.equals("finalize");
  }

  public static String createCallSignature(final MetaMethod m) {
    final StringBuilder append = new StringBuilder(m.getName()).append(':');
    for (final MetaParameter parm : m.getParameters()) {
      append.append(parm.getType().getCanonicalName()).append(':');
    }
    return append.toString();
  }

  public static String createCallSignature(final Class<?> referenceClass, final Method m) {
    final TypeToken<?> resolver = TypeToken.of(referenceClass);
    final StringBuilder append = new StringBuilder(m.getName()).append(':');
    for (final Type c : m.getGenericParameterTypes()) {
      final TypeToken<?> resolvedParamType = resolver.resolveType(c);
      append.append(resolvedParamType.getRawType().getCanonicalName()).append(':');
    }
    return append.toString();
  }

  public static boolean isMethodInInterface(final Class<?> iface, final Method member) {
    try {
      if (iface.getMethod(member.getName(), member.getParameterTypes()) != null) {
        return true;
      }
    } catch (final NoSuchMethodException e) {
    }
    return false;
  }

  public static boolean isMethodInInterface(final MetaClass iface, final MetaMethod member) {
    final MetaClass[] parameters = Arrays.stream(member.getParameters())
            .map(MetaParameter::getType)
            .toArray(MetaClass[]::new);

    return iface.getMethod(member.getName(), parameters) != null;
  }

  /**
   * Generates a valid return statement for the provided method.
   *
   * @param method
   * @return return statement for the provided method
   */
  public static Statement generateProxyMethodReturnStatement(final MetaMethod method) {
    Statement returnStatement = null;
    if (!method.getReturnType().equals(MetaClassFactory.get(void.class))) {

      // if it's a Number and not a BigDecimal or BigInteger
      if (MetaClassFactory.get(Number.class).isAssignableFrom(method.getReturnType().asBoxed())
              && method.getReturnType().asUnboxed().getFullyQualifiedName().indexOf('.') == -1) {

        if (MetaClassFactory.get(Double.class).isAssignableFrom(method.getReturnType().asBoxed())) {
          returnStatement = Stmt.load(0.0).returnValue();
        } else if (MetaClassFactory.get(Float.class).isAssignableFrom(method.getReturnType().asBoxed())) {
          returnStatement = Stmt.load(0f).returnValue();
        } else if (MetaClassFactory.get(Long.class).isAssignableFrom(method.getReturnType().asBoxed())) {
          returnStatement = Stmt.load(0l).returnValue();
        } else {
          returnStatement = Stmt.load(0).returnValue();
        }
      } else if (MetaClassFactory.get(char.class).equals(method.getReturnType())) {
        returnStatement = Stmt.load(0).returnValue();
      } else if (MetaClassFactory.get(Boolean.class).isAssignableFrom(method.getReturnType().asBoxed())) {
        returnStatement = Stmt.load(false).returnValue();
      } else {
        returnStatement = Stmt.load(null).returnValue();
      }
    }
    return returnStatement;
  }
}
