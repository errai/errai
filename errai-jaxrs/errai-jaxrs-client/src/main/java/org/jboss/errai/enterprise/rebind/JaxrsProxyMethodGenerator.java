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

package org.jboss.errai.enterprise.rebind;

import static org.jboss.errai.enterprise.rebind.TypeMarshaller.demarshal;
import static org.jboss.errai.enterprise.rebind.TypeMarshaller.marshal;

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.codegen.BlockStatement;
import org.jboss.errai.codegen.BooleanOperator;
import org.jboss.errai.codegen.DefParameters;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.StringStatement;
import org.jboss.errai.codegen.Variable;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.ContextualStatementBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.common.client.api.interceptor.InterceptedCall;
import org.jboss.errai.common.client.framework.CallContextStatus;
import org.jboss.errai.config.rebind.ProxyUtil;
import org.jboss.errai.enterprise.client.jaxrs.ResponseDemarshallingCallback;
import org.jboss.errai.enterprise.client.jaxrs.api.interceptor.RestCallContext;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Cookies;

/**
 * Generates a JAX-RS remote proxy method.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class JaxrsProxyMethodGenerator {
  private static final String APPEND = "append";

  private final MetaClass declaringClass;
  private final JaxrsResourceMethod resourceMethod;
  private final BlockBuilder<?> methodBlock;
  private final List<Statement> parameters;

  public JaxrsProxyMethodGenerator(final ClassStructureBuilder<?> classBuilder, final JaxrsResourceMethod resourceMethod) {
    this.declaringClass = classBuilder.getClassDefinition();
    this.resourceMethod = resourceMethod;

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

      if (resourceMethod.getMethod().isAnnotationPresent(InterceptedCall.class) ||
          resourceMethod.getMethod().getDeclaringClass().isAnnotationPresent(InterceptedCall.class)) {
        methodBlock.append(generateInterceptorLogic());
      }
      else {
        methodBlock.append(generateRequest());
      }
    }
    generateReturnStatement();
  }

  /**
   * Generates a {@link StringBuilder} constructing the request URL based on the method parameters annotated with JAX-RS
   * annotations.
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
      Statement pathParam = marshal(params.getPathParameter(pathParamName));
      if (params.needsEncoding(pathParamName)) {
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

    // construct query using @QueryParams
    if (params.getQueryParameters() != null) {
      urlBuilder = urlBuilder.invoke(APPEND, "?");

      int i = 0;
      for (String queryParamName : params.getQueryParameters().keySet()) {
        for (Statement queryParam : params.getQueryParameters(queryParamName)) {
          if (i++ > 0)
            urlBuilder = urlBuilder.invoke(APPEND, "&");

          urlBuilder = urlBuilder.invoke(APPEND, queryParamName).invoke(APPEND, "=")
              .invoke(APPEND, encodeQuery(marshal(queryParam)));
        }
      }
    }

    if (urlBuilder != null)
      block.addStatement(urlBuilder);

    return block;
  }

  /**
   * Generates the declaration for a new {@link RequestBuilder} instance, initialized with the generated URL
   * {@link #generateUrl(JaxrsResourceMethodParameters)}
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
   * {@link #generateRequestBuilder()}) based on the method parameters annotated with JAX-RS annotations.
   * 
   * @param params
   *          the resource method's parameters
   * @return a block statement with the corresponding calls to {@link RequestBuilder#setHeader(String, String)}
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
            .append(Stmt.invokeStatic(Cookies.class, "setCookie", cookieName, marshal(cookieParam)))
            .finish();

        block.addStatement(setCookie);
      }
    }
    return block.isEmpty() ? null : block;
  }

  /**
   * Generated the logic required to make this proxy method interceptable.
   * 
   * @return statement representing the interceptor logic.
   */
  private Statement generateInterceptorLogic() {
    JaxrsResourceMethodParameters jaxrsParams =
        JaxrsResourceMethodParameters.fromMethod(resourceMethod.getMethod(), "parameters");

    InterceptedCall interceptedCall = resourceMethod.getMethod().getAnnotation(InterceptedCall.class);
    if (interceptedCall == null) {
      interceptedCall = resourceMethod.getMethod().getDeclaringClass().getAnnotation(InterceptedCall.class);
    }

    Statement callContext =
        ProxyUtil.generateProxyMethodCallContext(RestCallContext.class, declaringClass,
            resourceMethod.getMethod(), generateInterceptedRequest(), interceptedCall)
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
                    Stmt.newObject(CallContextStatus.class).withParameters((Object[]) interceptedCall.value())))
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
            .append(Stmt.loadStatic(declaringClass, "this").invoke("handleError", Variable.get("throwable"), null, null))
            .finish();
  }

  /**
   * Generates the call to {@link RequestBuilder#sendRequest(String, com.google.gwt.http.client.RequestCallback)} for
   * interceptable methods.
   * 
   * @return statement representing the request
   */
  private Statement generateInterceptedRequest() {
    return generateRequest(Stmt.nestedCall(
        new StringStatement("getRequestBuilder()", MetaClassFactory.get(RequestBuilder.class))),
        Stmt.loadStatic(declaringClass, "this"));
  }

  /**
   * Generates the call to {@link RequestBuilder#sendRequest(String, com.google.gwt.http.client.RequestCallback)} for
   * non-interceptable methods.
   * 
   * @return statement representing the request
   */
  private Statement generateRequest() {
    return generateRequest(Stmt.loadVariable("requestBuilder"), Stmt.loadVariable("this"));
  }

  /**
   * Generates the call to {@link RequestBuilder#sendRequest(String, com.google.gwt.http.client.RequestCallback)} for
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
   * Generate an anonymous implementation/instance of {@link ResponseDemarshallingCallback} that will handle the
   * demarshalling of the HTTP response.
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
   * Generates the return statement of this proxy method if required. If the proxy method returns void, it will just
   * finish the method block.
   */
  private void generateReturnStatement() {
    Statement returnStatement = ProxyUtil.generateProxyMethodReturnStatement(resourceMethod.getMethod());
    if (returnStatement != null) {
      methodBlock.append(returnStatement);
    }

    methodBlock.finish();
  }
}