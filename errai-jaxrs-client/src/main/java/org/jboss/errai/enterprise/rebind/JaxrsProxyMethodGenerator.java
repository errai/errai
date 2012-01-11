/*
 * Copyright 2011 JBoss, a division of Red Hat, Inc
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

import java.util.List;

import org.jboss.errai.bus.rebind.RebindUtils;
import org.jboss.errai.codegen.framework.BooleanOperator;
import org.jboss.errai.codegen.framework.DefParameters;
import org.jboss.errai.codegen.framework.Parameter;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.Variable;
import org.jboss.errai.codegen.framework.builder.BlockBuilder;
import org.jboss.errai.codegen.framework.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.framework.builder.ContextualStatementBuilder;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.util.Bool;
import org.jboss.errai.codegen.framework.util.Stmt;
import org.jboss.errai.enterprise.client.jaxrs.api.ResponseCallback;
import org.jboss.errai.enterprise.client.jaxrs.api.ResponseException;
import org.jboss.resteasy.specimpl.UriBuilderImpl;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Cookies;

/**
 * Generates a JAX-RS remote proxy method.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class JaxrsProxyMethodGenerator {

  private static final String APPEND = "append";

  private MetaClass declaringClass;
  private JaxrsResourceMethod resourceMethod;
  private BlockBuilder<?> methodBlock;

  public JaxrsProxyMethodGenerator(ClassStructureBuilder<?> classBuilder, JaxrsResourceMethod resourceMethod) {
    this.declaringClass = classBuilder.getClassDefinition();
    this.resourceMethod = resourceMethod;
    this.methodBlock = classBuilder.publicMethod(resourceMethod.getMethod().getReturnType(),
        resourceMethod.getMethod().getName(),
        DefParameters.from(resourceMethod.getMethod()).getParameters().toArray(new Parameter[0]));
  }

  public void generate() {
    if (resourceMethod.getHttpMethod() != null) {
      generateUrl();
      generateRequestBuilder();
      generateHeaders();
      generateRequest();
    }

    generateReturnStatement();
  }

  private void generateUrl() {
    JaxrsResourceMethodParameters params = resourceMethod.getParameters();

    // construct path using @PathParams and @MatrixParams
    String path = resourceMethod.getPath();
    ContextualStatementBuilder pathValue = Stmt.loadLiteral(path);
    List<String> pathParams =
        ((UriBuilderImpl) UriBuilderImpl.fromTemplate("/" + path)).getPathParamNamesInDeclarationOrder();

    for (String pathParam : pathParams) {
      pathValue = pathValue.invoke("replaceAll", "\\{" + pathParam + "\\}",
          encodePath(marshal(params.getPathParameter(pathParam))));
    }

    if (params.getMatrixParameters() != null) {
      for (String matrixParamName : params.getMatrixParameters().keySet()) {
        pathValue = pathValue.invoke("concat", ";" + matrixParamName + "=")
            .invoke("concat", encodePath(marshal(params.getMatrixParameter(matrixParamName))));
      }
    }

    methodBlock.append(Stmt.declareVariable("url", StringBuilder.class, Stmt.newObject(StringBuilder.class)
        .withParameters(Stmt.loadVariable("this").invoke("getBaseUrl"))));

    // construct query using @QueryParams
    ContextualStatementBuilder urlBuilder = Stmt.loadVariable("url").invoke(APPEND, pathValue);
    if (params.getQueryParameters() != null) {
      urlBuilder = urlBuilder.invoke(APPEND, "?");

      int i = 0;
      for (String queryParamName : params.getQueryParameters().keySet()) {
        for (Parameter queryParam : params.getQueryParameters(queryParamName)) {
          if (i++ > 0)
            urlBuilder = urlBuilder.invoke(APPEND, "&");

          urlBuilder = urlBuilder.invoke(APPEND, queryParamName).invoke(APPEND, "=")
              .invoke(APPEND, encodeQuery(marshal(queryParam)));
        }
      }
    }

    if (urlBuilder != null)
      methodBlock.append(urlBuilder);
  }

  private void generateRequestBuilder() {
    Statement requestBuilder =
        Stmt.declareVariable("requestBuilder", RequestBuilder.class,
            Stmt.newObject(RequestBuilder.class)
                .withParameters(resourceMethod.getHttpMethod(), Stmt.loadVariable("url").invoke("toString")));

    methodBlock.append(requestBuilder);
  }

  private void generateHeaders() {
    JaxrsResourceMethodParameters params = resourceMethod.getParameters();

    // set headers based on method and class
    for (String key : resourceMethod.getHeaders().keySet()) {
      methodBlock.append(Stmt.loadVariable("requestBuilder").invoke("setHeader", key,
          resourceMethod.getHeaders().get(key)));
    }

    // set headers based on @HeaderParams
    if (params.getHeaderParameters() != null) {
      for (String headerParamName : params.getHeaderParameters().keySet()) {
        ContextualStatementBuilder headerValueBuilder = Stmt.nestedCall(Stmt.newObject(StringBuilder.class));

        int i = 0;
        for (Parameter headerParam : params.getHeaderParameters(headerParamName)) {
          if (i++ > 0) {
            headerValueBuilder = headerValueBuilder.invoke(APPEND, ",");
          }
          headerValueBuilder = headerValueBuilder.invoke(APPEND, marshal(headerParam));
        }

        methodBlock.append(Stmt.loadVariable("requestBuilder").invoke("setHeader", headerParamName,
            headerValueBuilder.invoke("toString")));
      }
    }

    // set cookies based on @CookieParams
    if (params.getCookieParameters() != null) {
      for (String cookieName : params.getCookieParameters().keySet()) {
        Parameter cookieParam = params.getCookieParameters().get(cookieName).get(0);

        Statement setCookie = Stmt.loadVariable(cookieParam.getName())
            .if_(BooleanOperator.NotEquals, null)
              .append(Stmt.invokeStatic(Cookies.class, "setCookie", cookieName, marshal(cookieParam)))
            .finish();

        methodBlock.append(setCookie);
      }
    }
  }

  private void generateRequest() {
    ContextualStatementBuilder sendRequest = Stmt.loadVariable("requestBuilder");
    if (resourceMethod.getParameters().getEntityParameter() == null) {
      sendRequest = sendRequest.invoke("sendRequest", null, createRequestCallback());
    }
    else {
      Statement body = marshal(resourceMethod.getParameters().getEntityParameter());
      sendRequest = sendRequest.invoke("sendRequest", body, createRequestCallback());
    }

    methodBlock.append(Stmt
        .try_()
        .append(sendRequest)
        .finish()
        .catch_(RequestException.class, "throwable")
        .append(errorHandling())
        .finish());
  }

  private void generateReturnStatement() {
    Statement returnStatement = RebindUtils.generateProxyMethodReturnStatement(resourceMethod.getMethod());
    if (returnStatement != null) {
      methodBlock.append(returnStatement);
    }

    methodBlock.finish();
  }

  private Statement createRequestCallback() {
    Statement requestCallback = Stmt
        .newObject(RequestCallback.class)
        .extend()
        .publicOverridesMethod("onError", Parameter.of(Request.class, "request"),
            Parameter.of(Throwable.class, "throwable"))
        .append(errorHandling())
        .finish()
        .publicOverridesMethod("onResponseReceived", Parameter.of(Request.class, "request"),
            Parameter.of(Response.class, "response"))
        .append(Stmt.if_(Bool.and(
                Bool.greaterThanOrEqual(Stmt.loadVariable("response").invoke("getStatusCode"), 200),
                Bool.lessThan(Stmt.loadVariable("response").invoke("getStatusCode"), 300)))
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

    Statement response = demarshal(resourceMethod.getMethod().getReturnType(),
        Stmt.loadVariable("response").invoke("getText"));

    Statement handleResult = Stmt
        .if_(Bool.equals(Stmt.loadVariable("response").invoke("getStatusCode"), 204))
        .append(Stmt.loadStatic(declaringClass, "this").loadField("remoteCallback").invoke("callback", Stmt.load(null)))
        .finish()
        .else_()
        .append(Stmt.loadStatic(declaringClass, "this").loadField("remoteCallback").invoke("callback", response))
        .finish();

    return Stmt
        .if_(Bool.instanceOf(Stmt.loadStatic(declaringClass, "this").loadField("remoteCallback"),
            MetaClassFactory.getAsStatement(ResponseCallback.class)))
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
}