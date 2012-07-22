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

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Cookies;
import org.jboss.errai.bus.client.api.interceptor.InterceptedCall;
import org.jboss.errai.bus.client.framework.CallContextStatus;
import org.jboss.errai.bus.rebind.RebindUtils;
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
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.util.Bool;
import org.jboss.errai.codegen.util.If;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.enterprise.client.jaxrs.api.ResponseCallback;
import org.jboss.errai.enterprise.client.jaxrs.api.ResponseException;
import org.jboss.errai.enterprise.client.jaxrs.api.interceptor.RestCallContext;

import java.util.ArrayList;
import java.util.List;

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

  public JaxrsProxyMethodGenerator(ClassStructureBuilder<?> classBuilder, JaxrsResourceMethod resourceMethod) {
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
      methodBlock.append(generateRequestBuilder(jaxrsParams));
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

  private Statement generateUrl(JaxrsResourceMethodParameters params) {
    BlockStatement block = new BlockStatement();
    block.addStatement(Stmt.declareVariable("url", StringBuilder.class,
        Stmt.newObject(StringBuilder.class, new StringStatement("getBaseUrl()"))));

    // construct path using @PathParams and @MatrixParams
    String path = resourceMethod.getPath();
    ContextualStatementBuilder pathValue = Stmt.loadLiteral(path);

    for (String pathParamName : JaxrsResourceMethodParameters.getPathParameterNames(path)) {
      pathValue = pathValue.invoke("replace", "{" + pathParamName + "}",
          encodePath(marshal(params.getPathParameter(pathParamName))));
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

  private Statement generateRequestBuilder(JaxrsResourceMethodParameters params) {
    Statement requestBuilder =
        Stmt.declareVariable("requestBuilder", RequestBuilder.class,
            Stmt.newObject(RequestBuilder.class)
                .withParameters(resourceMethod.getHttpMethod(), Stmt.loadVariable("url").invoke("toString")));

    return requestBuilder;
  }

  private Statement generateHeaders(JaxrsResourceMethodParameters params) {
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

  private Statement generateInterceptorLogic() {
    JaxrsResourceMethodParameters jaxrsParams =
        JaxrsResourceMethodParameters.fromMethod(resourceMethod.getMethod(), "parameters");

    InterceptedCall interceptedCall = resourceMethod.getMethod().getAnnotation(InterceptedCall.class);
    if (interceptedCall == null) {
      interceptedCall = resourceMethod.getMethod().getDeclaringClass().getAnnotation(InterceptedCall.class);
    }

    Statement callContext =
        RebindUtils.generateProxyMethodCallContext(RestCallContext.class, declaringClass,
            resourceMethod.getMethod(), generateInterceptedRequest(), interceptedCall)
            .publicOverridesMethod("setParameters", Parameter.of(Object[].class, "parameters"))
            .append(new StringStatement("super.setParameters(parameters)"))
            .append(generateUrl(jaxrsParams))
            .append(generateRequestBuilder(jaxrsParams))
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
            .append(errorHandling())
            .finish();
  }

  private Statement generateInterceptedRequest() {
    return generateRequest(Stmt.nestedCall(
        new StringStatement("getRequestBuilder()", MetaClassFactory.get(RequestBuilder.class))));
  }

  private Statement generateRequest() {
    return generateRequest(Stmt.loadVariable("requestBuilder"));
  }

  private Statement generateRequest(ContextualStatementBuilder requestBuilder) {
    if (resourceMethod.getParameters().getEntityParameter() == null) {
      requestBuilder = requestBuilder.invoke("sendRequest", null, createRequestCallback());
    }
    else {
      Statement body = marshal(resourceMethod.getParameters().getEntityParameter(),
          resourceMethod.getContentTypeHeader());
      requestBuilder = requestBuilder.invoke("sendRequest", body, createRequestCallback());
    }

    return Stmt
        .try_()
        .append(requestBuilder)
        .finish()
        .catch_(RequestException.class, "throwable")
        .append(errorHandling())
        .finish();
  }

  private Statement createRequestCallback() {
    Statement statusCode = Stmt.loadVariable("response").invoke("getStatusCode");

    Statement requestCallback =
        Stmt
            .newObject(RequestCallback.class)
            .extend()
            .publicOverridesMethod("onError", Parameter.of(Request.class, "request"),
                Parameter.of(Throwable.class, "throwable"))
            .append(errorHandling())
            .finish()
            .publicOverridesMethod("onResponseReceived", Parameter.of(Request.class, "request"),
                Parameter.of(Response.class, "response"))
            .append(
                Stmt.if_(
                    Bool.and(
                        Bool.or(
                            Bool.isNull(Stmt.loadStatic(declaringClass, "this").loadField("successCodes")),
                            Stmt.loadStatic(declaringClass, "this").loadField("successCodes").invoke("contains",
                                statusCode)
                            ),
                        Bool.and(
                            Bool.greaterThanOrEqual(statusCode, 200),
                            Bool.lessThan(statusCode, 300))
                        )
                    )
                    .append(responseHandling())
                    .finish()
                    .else_()
                    .append(Stmt.declareVariable("throwable", ResponseException.class,
                        Stmt.newObject(ResponseException.class).withParameters(
                            Stmt.loadVariable("response").invoke("getStatusText"), Variable.get("response"))))
                    .append(responseErrorHandling())
                    .finish())
            .finish()
            .finish();

    return requestCallback;
  }

  private Statement errorHandling() {
    return Stmt.loadStatic(declaringClass, "this").invoke("handleError", Variable.get("throwable"), null);
  }

  private Statement responseErrorHandling() {
    return Stmt.loadStatic(declaringClass, "this")
            .invoke("handleError", Variable.get("throwable"), Variable.get("response"));
  }

  private Statement responseHandling() {
    Statement handleResponse = Stmt.loadStatic(declaringClass, "this").loadField("remoteCallback")
        .invoke("callback", Stmt.loadVariable("response"));

    Statement result = null;
    if (resourceMethod.getMethod().getReturnType().equals(MetaClassFactory.get(void.class))) {
      result = Stmt.load(null);
    }
    else {
      result = demarshal(resourceMethod.getMethod().getReturnType(),
          Stmt.loadVariable("response").invoke("getText"), resourceMethod.getAcceptHeader());
    }

    Statement handleResult =
        If.idEquals(Stmt.loadVariable("response").invoke("getStatusCode"), 204)
            .append(
                Stmt.loadStatic(declaringClass, "this").loadField("remoteCallback").invoke("callback", Stmt.load(null)))
            .finish()
            .else_()
            .append(Stmt.loadStatic(declaringClass, "this").loadField("remoteCallback").invoke("callback", result))
            .finish();

    return
        If.instanceOf(Stmt.loadStatic(declaringClass, "this").loadField("remoteCallback"), ResponseCallback.class)
        .append(handleResponse)
        .finish()
        .else_()
        .append(handleResult)
        .finish();
  }

  private Statement encodePath(Statement s) {
    return Stmt.invokeStatic(URL.class, "encodePathSegment", s);
  }

  private Statement encodeQuery(Statement s) {
    return Stmt.invokeStatic(URL.class, "encodeQueryString", s);
  }

  private void generateReturnStatement() {
    Statement returnStatement = RebindUtils.generateProxyMethodReturnStatement(resourceMethod.getMethod());
    if (returnStatement != null) {
      methodBlock.append(returnStatement);
    }

    methodBlock.finish();
  }
}