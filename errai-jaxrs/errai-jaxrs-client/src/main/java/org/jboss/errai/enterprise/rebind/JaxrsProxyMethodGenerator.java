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

package org.jboss.errai.enterprise.rebind;

import static org.jboss.errai.enterprise.rebind.TypeMarshaller.demarshal;
import static org.jboss.errai.enterprise.rebind.TypeMarshaller.marshal;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ws.rs.QueryParam;

import org.jboss.errai.codegen.BlockStatement;
import org.jboss.errai.codegen.BooleanOperator;
import org.jboss.errai.codegen.Cast;
import org.jboss.errai.codegen.DefParameters;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.StringStatement;
import org.jboss.errai.codegen.Variable;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.ContextualStatementBuilder;
import org.jboss.errai.codegen.builder.impl.BooleanExpressionBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.exception.GenerationException;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaParameterizedType;
import org.jboss.errai.codegen.meta.MetaType;
import org.jboss.errai.codegen.util.If;
import org.jboss.errai.codegen.util.ProxyUtil;
import org.jboss.errai.codegen.util.ProxyUtil.InterceptorProvider;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.framework.CallContextStatus;
import org.jboss.errai.enterprise.client.jaxrs.ResponseDemarshallingCallback;
import org.jboss.errai.enterprise.client.jaxrs.api.ResponseCallback;
import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;
import org.jboss.errai.enterprise.client.jaxrs.api.interceptor.RestCallContext;
import org.jboss.errai.enterprise.rebind.TypeMarshaller.PrimitiveTypeMarshaller;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;

/**
 * Generates a JAX-RS remote proxy method.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class JaxrsProxyMethodGenerator {
  private static final String APPEND = "append";

  private final MetaClass remote;
  private final MetaClass declaringClass;
  private final JaxrsResourceMethod resourceMethod;
  private final BlockBuilder<?> methodBlock;
  private final List<Statement> parameters;
  private final InterceptorProvider interceptorProvider;
  private final GeneratorContext context;

  public JaxrsProxyMethodGenerator(final MetaClass remote, 
      final ClassStructureBuilder<?> classBuilder, 
      final JaxrsResourceMethod resourceMethod, 
      final InterceptorProvider interceptorProvider,
      final GeneratorContext context) {
    
    this.remote = remote;
    this.declaringClass = classBuilder.getClassDefinition();
    this.resourceMethod = resourceMethod;
    this.interceptorProvider = interceptorProvider;
    this.context = context;
    
    Parameter[] parms = DefParameters.from(resourceMethod.getMethod()).getParameters().toArray(new Parameter[0]);
    Parameter[] finalParms = new Parameter[parms.length];
    parameters = new ArrayList<Statement>();
    for (int i = 0; i < parms.length; i++) {
      finalParms[i] = Parameter.of(parms[i].getType(), parms[i].getName(), true);
      parameters.add(Stmt.loadVariable(parms[i].getName()));
    }
    this.methodBlock = classBuilder.publicMethod(resourceMethod.getMethod().getReturnType(),
        resourceMethod.getMethod().getName(), finalParms);
  }

  public void generate() {
    if (resourceMethod.getHttpMethod() != null) {
      JaxrsResourceMethodParameters jaxrsParams = resourceMethod.getParameters();
      methodBlock.append(generateUrl(jaxrsParams));
      methodBlock.append(generateRequestBuilder());
      methodBlock.append(generateHeaders(jaxrsParams));

      List<Class<?>> interceptors = interceptorProvider.getInterceptors(remote, resourceMethod.getMethod());
      if (!interceptors.isEmpty()) {
        methodBlock.append(generateInterceptorLogic(interceptors));
      }
      else {
        methodBlock.append(generateRequest());
      }
    }
    generateReturnStatement();
  }

  /**
   * Generates a {@link StringBuilder} constructing the request URL based on the method parameters
   * annotated with JAX-RS annotations.
   * 
   * @param params
   *          the resource method's parameters
   * @return the URL statement
   */
  private Statement generateUrl(final JaxrsResourceMethodParameters params) {
    BlockStatement block = new BlockStatement();
    block.addStatement(Stmt.declareVariable("url", StringBuilder.class,
        Stmt.newObject(StringBuilder.class, new StringStatement("getBaseUrl()"))));

    // construct path using @PathParams and @MatrixParams
    String path = resourceMethod.getPath();
    ContextualStatementBuilder pathValue = Stmt.loadLiteral(path);

    for (String pathParamName : JaxrsResourceMethodParameters.getPathParameterNames(path)) {
      String pathParamId = pathParamName;
      if (pathParamName.contains(":")) {
        pathParamId = pathParamName.split(":")[0];
      }
      Statement pathParam = marshal(params.getPathParameter(pathParamId));
      if (params.needsEncoding(pathParamId)) {
        pathParam = encodePath(pathParam);
      }
      pathValue = pathValue.invoke("replace", "{" + pathParamName + "}", pathParam);
    }

    if (params.getMatrixParameters() != null) {
      for (String matrixParamName : params.getMatrixParameters().keySet()) {
        pathValue = pathValue.invoke("concat", ";" + matrixParamName + "=")
            .invoke("concat", encodePath(marshal(params.getMatrixParameter(matrixParamName))));
      }
    }

    ContextualStatementBuilder urlBuilder = Stmt.loadVariable("url").invoke(APPEND, pathValue);
    block.addStatement(urlBuilder);

    // construct query using @QueryParams
    if (params.getQueryParameters() != null) {
      urlBuilder = urlBuilder.invoke(APPEND, "?");
      int i = 0;
      for (String queryParamName : params.getQueryParameters().keySet()) {
        for (Statement queryParam : params.getQueryParameters(queryParamName)) {

          MetaClass queryParamType = queryParam.getType();
          if (isListOrSet(queryParamType)) {
            MetaClass paramType = assertValidCollectionParam(queryParamType, queryParamName, QueryParam.class);
            ContextualStatementBuilder listParam = (queryParam instanceof Parameter) ?
                Stmt.loadVariable(((Parameter) queryParam).getName()) : Stmt.nestedCall(queryParam);

            block.addStatement(listParam.foreachIfNotNull("p")
                .append(If.not(Stmt.loadVariable("url").invoke("toString").invoke("endsWith", "?"))
                    .append(Stmt.loadVariable("url").invoke(APPEND, "&")).finish())
                .append(Stmt.loadVariable("url").invoke(APPEND, queryParamName).invoke(APPEND, "=")
                    .invoke(APPEND, encodeQuery(marshal(paramType, Stmt.loadVariable("p")))))
                .finish()
                );
          }
          else {
            if (i++ > 0) {
              urlBuilder = urlBuilder.invoke(APPEND, "&");
            }
            urlBuilder = urlBuilder.invoke(APPEND, queryParamName).invoke(APPEND, "=")
                .invoke(APPEND, encodeQuery(marshal(queryParam)));
          }
        }
      }
    }

    return block;
  }

  /**
   * Checks if the provided type is a {@link List} or {@link Set}.
   * 
   * @param paramType
   *          the type to check
   * @return true if the type can be assigned to a List or Set, otherwise false.
   */
  private boolean isListOrSet(MetaClass paramType) {
    return paramType.isAssignableTo(List.class) || paramType.isAssignableTo(Set.class);
  }

  /**
   * Asserts that the provided type is a valid collection type for JAX-RS resource parameters.
   * 
   * @param paramType
   *          the provided type.
   * @param paramName
   *          the name of the resource parameter for error reporting.
   * @param jaxrsParamType
   *          the JAX-RS resource parameter type for error reporting.
   * 
   * @return the element type.
   * @throws GenerationException
   *           if the type parameters of the collection type are invalid for JAX-RS resource
   *           parameters.
   */
  private MetaClass assertValidCollectionParam(MetaClass paramType, String paramName, Class<?> jaxrsParamType) {
    MetaParameterizedType queryParamPType = paramType.getParameterizedType();
    MetaType[] typeParams = (queryParamPType != null) ? queryParamPType.getTypeParameters() : null;
    if (typeParams != null && typeParams.length == 1 && typeParams[0] instanceof MetaClass
          && PrimitiveTypeMarshaller.canHandle((MetaClass) typeParams[0], "text/plain")) {

      return (MetaClass) typeParams[0];

    }
    else {
      throw new GenerationException(
            "Unsupported type parameter found on " + jaxrsParamType.getSimpleName() + " with name "
                + paramName + " in method " + resourceMethod.getMethod() +
                " (check the JavaDocs of " + jaxrsParamType.getName() + " for details!)");
    }
  }

  /**
   * Generates the declaration for a new {@link RequestBuilder} instance, initialized with the
   * generated URL {@link #generateUrl(JaxrsResourceMethodParameters)}
   * 
   * @return the RequestBuilder statement
   */
  private Statement generateRequestBuilder() {
    Statement requestBuilder =
        Stmt.declareVariable("requestBuilder", RequestBuilder.class,
            Stmt.newObject(RequestBuilder.class)
                .withParameters(resourceMethod.getHttpMethod(), Stmt.loadVariable("url").invoke("toString")));

    return requestBuilder;
  }

  /**
   * Generates calls to set the appropriate headers on the generated {@link RequestBuilder} (see
   * {@link #generateRequestBuilder()}) based on the method parameters annotated with JAX-RS
   * annotations.
   * 
   * @param params
   *          the resource method's parameters
   * @return a block statement with the corresponding calls to
   *         {@link RequestBuilder#setHeader(String, String)}
   */
  private Statement generateHeaders(final JaxrsResourceMethodParameters params) {
    BlockStatement block = new BlockStatement();

    // set headers based on method and class
    for (String key : resourceMethod.getHeaders().keySet()) {
      block.addStatement(Stmt.loadVariable("requestBuilder").invoke("setHeader", key,
          resourceMethod.getHeaders().get(key)));
    }

    // set headers based on @HeaderParams
    if (params.getHeaderParameters() != null) {
      for (String headerParamName : params.getHeaderParameters().keySet()) {
        ContextualStatementBuilder headerValueBuilder = Stmt.nestedCall(Stmt.newObject(StringBuilder.class));

        int i = 0;
        for (Statement headerParam : params.getHeaderParameters(headerParamName)) {
          if (i++ > 0) {
            headerValueBuilder = headerValueBuilder.invoke(APPEND, ",");
          }
          headerValueBuilder = headerValueBuilder.invoke(APPEND, marshal(headerParam));
        }

        block.addStatement(Stmt.loadVariable("requestBuilder").invoke("setHeader", headerParamName,
            headerValueBuilder.invoke("toString")));
      }
    }

    // set cookies based on @CookieParams
    if (params.getCookieParameters() != null) {
      for (String cookieName : params.getCookieParameters().keySet()) {
        Statement cookieParam = params.getCookieParameters().get(cookieName).get(0);

        ContextualStatementBuilder setCookie = (cookieParam instanceof Parameter) ?
            Stmt.loadVariable(((Parameter) cookieParam).getName()) :
              Stmt.nestedCall(cookieParam);

        setCookie.if_(BooleanOperator.NotEquals, null)
            .append(Stmt.invokeStatic(RestClient.class, "setCookie", cookieName, marshal(cookieParam)))
            .finish();

        block.addStatement(setCookie);
      }
    }
    return block.isEmpty() ? null : block;
  }

  /**
   * Generates the logic required to make this proxy method interceptable.
   * 
   * @param interceptors
   * @return statement representing the interceptor logic.
   */
  private Statement generateInterceptorLogic(List<Class<?>> interceptors) {
    JaxrsResourceMethodParameters jaxrsParams =
        JaxrsResourceMethodParameters.fromMethod(resourceMethod.getMethod(), "parameters");
    
    Statement callContext =
        ProxyUtil.generateProxyMethodCallContext(context, RestCallContext.class, declaringClass,
            resourceMethod.getMethod(), generateInterceptedRequest(), interceptors)
            .publicOverridesMethod("proceed", Parameter.of(ResponseCallback.class, "interceptorCallback", true))
              .append(Stmt.declareVariable(RemoteCallback.class).asFinal().named("providedCallback").initializeWith(
                  Stmt.loadStatic(declaringClass, "this").loadField("remoteCallback")))
              .append(
                  Stmt.loadVariable("remoteCallback").assignValue(Stmt.newObject(ResponseCallback.class).extend()
                      .publicOverridesMethod("callback", Parameter.of(Response.class, "response"))
                      .append(Stmt.declareVariable(ResponseCallback.class).named("intCallback")
                          .initializeWith(Stmt.loadVariable("interceptorCallback")))
                      .append(StringStatement.of("setResult(response)"))
                      .append(Stmt.loadVariable("intCallback").invoke("callback",
                          Cast.to(Response.class, StringStatement.of("getResult()", Object.class))))
                      .append(Stmt.loadVariable("providedCallback").invoke("callback",
                          StringStatement.of("getResult()", Object.class)))
                      .finish()
                      .finish())
              )
              .append(Stmt.loadVariable("this").invoke("proceed"))
            .finish()
            .publicOverridesMethod("proceed", Parameter.of(ResponseCallback.class, "interceptorCallback", true), 
                    Parameter.of(ErrorCallback.class, "interceptorErrorCallback", true))
              .append(
                  Stmt.declareVariable(ErrorCallback.class).asFinal().named("providedErrorCallback").initializeWith(
                      Stmt.loadStatic(declaringClass, "this").loadField("errorCallback")))
              .append(
                  Stmt.loadVariable("errorCallback").assignValue(Stmt.newObject(ErrorCallback.class).extend()
                      .publicOverridesMethod("error", Parameter.of(Object.class, "message"),
                          Parameter.of(Throwable.class, "throwable"))
                      .append(
                          Stmt.loadVariable("interceptorErrorCallback").invoke("error", Variable.get("message"),
                              Variable.get("throwable")))
                      .append(
                          Stmt.if_(
                              BooleanExpressionBuilder.create(
                                 StringStatement.of("getResult()", Object.class), BooleanOperator.NotEquals, Stmt
                                      .loadLiteral(null)))
                              .append(
                                  Stmt.loadVariable("remoteCallback").invoke("callback",
                                          StringStatement.of("getResult()", Object.class)))
                              .append(Stmt.load(false).returnValue())
                              .finish()
                          .elseif_(
                              BooleanExpressionBuilder.create(
                                  Stmt.loadVariable("providedErrorCallback"), BooleanOperator.NotEquals, Stmt
                                      .loadLiteral(null)))
                              .append(
                                  Stmt.nestedCall(
                                    Stmt.loadVariable("providedErrorCallback").invoke("error",
                                      Variable.get("message"),
                                      Variable.get("throwable")))
                                  .returnValue())
                              .finish())
                      .append(Stmt.load(true).returnValue())
                      .finish()
                      .finish())
              )
              .append(Stmt.loadVariable("this").invoke("proceed", Variable.get("interceptorCallback")))
            .finish()
            .publicOverridesMethod("setParameters", Parameter.of(Object[].class, "parameters"))
              .append(new StringStatement("super.setParameters(parameters)"))
              .append(generateUrl(jaxrsParams))
              .append(generateRequestBuilder())
              .append(generateHeaders(jaxrsParams))
              .append(new StringStatement("setRequestBuilder(requestBuilder)"))
            .finish()
            .finish();

    return Stmt.try_()
            .append(
                Stmt.declareVariable(CallContextStatus.class).asFinal().named("status").initializeWith(
                    Stmt.newObject(CallContextStatus.class).withParameters(interceptors.toArray())))
            .append(
                Stmt.declareVariable(RestCallContext.class).asFinal().named("callContext")
                    .initializeWith(callContext))
            .append(
                Stmt.loadVariable("callContext").invoke("setRequestBuilder", Variable.get("requestBuilder")))
            .append(
                Stmt.loadVariable("callContext").invoke("setParameters",
                    Stmt.newArray(Object.class).initialize(parameters.toArray())))
            .append(
                Stmt.loadVariable("callContext").invoke("proceed"))
            .finish()
            .catch_(Throwable.class, "throwable")
            .append(
                Stmt.loadStatic(declaringClass, "this").invoke("handleError", Variable.get("throwable"), null, null))
            .finish();
  }

  /**
   * Generates the call to
   * {@link RequestBuilder#sendRequest(String, com.google.gwt.http.client.RequestCallback)} for
   * interceptable methods.
   * 
   * @return statement representing the request
   */
  private Statement generateInterceptedRequest() {
    return generateRequest(
            Stmt.nestedCall(new StringStatement("getRequestBuilder()", MetaClassFactory.get(RequestBuilder.class))),
            Stmt.loadStatic(declaringClass, "this"));
  }

  /**
   * Generates the call to
   * {@link RequestBuilder#sendRequest(String, com.google.gwt.http.client.RequestCallback)} for
   * non-interceptable methods.
   * 
   * @return statement representing the request
   */
  private Statement generateRequest() {
    return generateRequest(Stmt.loadVariable("requestBuilder"), Stmt.loadVariable("this"));
  }

  /**
   * Generates the call to
   * {@link RequestBuilder#sendRequest(String, com.google.gwt.http.client.RequestCallback)} for
   * proxy methods.
   * 
   * @return statement representing the request
   */
  private Statement generateRequest(final ContextualStatementBuilder requestBuilder, 
          final ContextualStatementBuilder proxy) {
    
    Statement sendRequest = null;
    if (resourceMethod.getParameters().getEntityParameter() == null) {
      sendRequest = proxy.invoke("sendRequest", requestBuilder, null, responseDemarshallingCallback());
    }
    else {
      Statement body = marshal(resourceMethod.getParameters().getEntityParameter(),
          resourceMethod.getContentTypeHeader());
      sendRequest = proxy.invoke("sendRequest", requestBuilder, body, responseDemarshallingCallback());
    }

    return sendRequest;
  }

  /**
   * Generates an anonymous implementation/instance of {@link ResponseDemarshallingCallback} that
   * will handle the demarshalling of the HTTP response.
   * 
   * @return statement representing the {@link ResponseDemarshallingCallback}.
   */
  private Statement responseDemarshallingCallback() {
    Statement result = Stmt.load(null);
    if (!resourceMethod.getMethod().getReturnType().equals(MetaClassFactory.get(void.class))) {
      result = demarshal(
          resourceMethod.getMethod().getReturnType(),
          Stmt.loadVariable("response"),
          resourceMethod.getAcceptHeader());
    }

    return ObjectBuilder
        .newInstanceOf(ResponseDemarshallingCallback.class)
        .extend()
        .publicOverridesMethod("demarshallResponse", Parameter.of(String.class, "response"))
        .append(Stmt.nestedCall(result).returnValue())
        .finish()
        .finish();
  }

  /**
   * Adds path encoding to the provided statement
   * 
   * @param s
   *          the statement representing an HTTP path that should be encoded
   * @return statement with path encoding
   */
  private Statement encodePath(Statement s) {
    return Stmt.invokeStatic(URL.class, "encodePathSegment", s);
  }

  /**
   * Adds query parameter encoding to the provided statement
   * 
   * @param s
   *          the statement representing an HTTP query that should be encoded
   * @return statement with query param encoding
   */
  private Statement encodeQuery(Statement s) {
    return Stmt.invokeStatic(URL.class, "encodeQueryString", s);
  }

  /**
   * Generates the return statement of this proxy method if required. If the proxy method returns
   * void, it will just finish the method block.
   */
  private void generateReturnStatement() {
    Statement returnStatement = ProxyUtil.generateProxyMethodReturnStatement(resourceMethod.getMethod());
    if (returnStatement != null) {
      methodBlock.append(returnStatement);
    }

    methodBlock.finish();
  }
}
