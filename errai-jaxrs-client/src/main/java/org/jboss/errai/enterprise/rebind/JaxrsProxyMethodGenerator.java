/*
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

import org.jboss.errai.codegen.framework.DefParameters;
import org.jboss.errai.codegen.framework.Parameter;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.Variable;
import org.jboss.errai.codegen.framework.builder.BlockBuilder;
import org.jboss.errai.codegen.framework.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.framework.builder.ContextualStatementBuilder;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.meta.MetaMethod;
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

/**
 * Generates a JAX-RS remote proxy method.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class JaxrsProxyMethodGenerator {
  private static final String APPEND = "append";

  private JaxrsResourceMethod resourceMethod;
  private MetaClass clazz;
  
  private Statement responseHandling;
  private Statement errorHandlingWithResponse;
  private Statement errorHandling;
  private Statement dummyResponse;
  
  public JaxrsProxyMethodGenerator(JaxrsResourceMethod resourceMethod) {
    this.resourceMethod = resourceMethod;
  }

  public void generate(ClassStructureBuilder<?> classBuilder) {
    this.clazz = classBuilder.getClassDefinition();
    
    MetaMethod method = resourceMethod.getMethod();
    
    // TODO remove as soon as codegen bug is fixed (passing null as a method param is currently not working)
    dummyResponse = Stmt.declareVariable("response", Response.class, null);
    errorHandling = Stmt.loadStatic(clazz, "this").invoke("handleError", Variable.get("throwable"), Variable.get("response"));
    
    errorHandlingWithResponse = Stmt.loadStatic(clazz, "this").invoke("handleError", 
        Variable.get("throwable"), Variable.get("response"));
   
    Statement handleResponse = Stmt.loadStatic(clazz, "this").loadField("remoteCallback")
      .invoke("callback", Stmt.loadVariable("response"));

    Statement handleResult = Stmt.loadStatic(clazz, "this").loadField("remoteCallback")
      .invoke("callback", demarshal(method.getReturnType(), Stmt.loadVariable("response").invoke("getText")));

    this.responseHandling = Stmt.if_(Bool.instanceOf(
        Stmt.loadStatic(clazz, "this").loadField("remoteCallback"), 
          MetaClassFactory.getAsStatement(ResponseCallback.class)))
        .append(handleResponse)
        .finish()
        .else_()
        .append(handleResult)
        .finish();
                
    BlockBuilder<?> methodBlock =
        classBuilder.publicMethod(method.getReturnType(), method.getName(),
            DefParameters.from(method).getParameters().toArray(new Parameter[0]));

    if (resourceMethod.getHttpMethod() != null) {
      generateUrl(methodBlock);
      generateRequestBuilder(methodBlock);
      generateHeaders(methodBlock);
      generateRequest(methodBlock);
    }

    generateReturnStatement(methodBlock, method);
  }

  private void generateUrl(BlockBuilder<?> methodBlock) {
    JaxrsResourceMethodParameters params = resourceMethod.getParameters();
    ContextualStatementBuilder pathValue = Stmt.loadLiteral(resourceMethod.getPath());

    String path =  resourceMethod.getPath();
    if (!path.startsWith("/"))
     path = "/" + path;

    List<String> pathParams =
        ((UriBuilderImpl) UriBuilderImpl.fromTemplate(path)).getPathParamNamesInDeclarationOrder();

    for (String pathParam : pathParams) {
      pathValue = pathValue.invoke("replaceAll", "\\{" + pathParam + "\\}", 
          marshal(params.getPathParameter(pathParam)));
    }

    methodBlock.append(Stmt.declareVariable("url", StringBuilder.class,
        Stmt.newObject(StringBuilder.class).withParameters(pathValue)));

    ContextualStatementBuilder urlBuilder = null;
    if (params.getQueryParameters() != null) {
      urlBuilder = Stmt.loadVariable("url").invoke(APPEND, "?");

      int i = 0;
      for (String queryParamName : params.getQueryParameters().keySet()) {
        for (Parameter queryParam : params.getQueryParameters(queryParamName)) {
          if (i++ > 0)
            urlBuilder = urlBuilder.invoke(APPEND, "&");

          urlBuilder = urlBuilder.invoke(APPEND, queryParamName);
          urlBuilder = urlBuilder.invoke(APPEND, "=");
          urlBuilder = urlBuilder.invoke(APPEND, marshal(queryParam));
        }
      }
    }
    
    // TODO MatrixParams
    if (urlBuilder != null)
      methodBlock.append(urlBuilder);
  }

  private void generateHeaders(BlockBuilder<?> methodBlock) {
    JaxrsResourceMethodParameters params = resourceMethod.getParameters();

    for (String key : resourceMethod.getHeaders().keySet()) {
      methodBlock.append(Stmt.loadVariable("requestBuilder").invoke("setHeader", key, 
          resourceMethod.getHeaders().get(key)));
    }
    
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
    
    // TODO CookieParams
  }

  private void generateRequestBuilder(BlockBuilder<?> methodBlock) {
    Statement urlEncoder = Stmt.invokeStatic(URL.class, "encode", Stmt.loadVariable("url").invoke("toString"));

    Statement requestBuilder =
        Stmt.declareVariable("requestBuilder", RequestBuilder.class,
            Stmt.newObject(RequestBuilder.class)
                .withParameters(resourceMethod.getHttpMethod(), urlEncoder));

    methodBlock.append(requestBuilder);
  }

  private void generateRequest(BlockBuilder<?> methodBlock) {
    ContextualStatementBuilder sendRequest = Stmt.loadVariable("requestBuilder");
    if (resourceMethod.getParameters().getEntityParameter() == null) {
      sendRequest = sendRequest.invoke("sendRequest", null, generateRequestCallback());
    }
    else {
      Statement body = marshal(resourceMethod.getParameters().getEntityParameter());
      sendRequest = sendRequest.invoke("sendRequest", body, generateRequestCallback());
    }

    methodBlock.append(Stmt
        .try_()
        .append(sendRequest)
        .finish()
        .catch_(RequestException.class, "throwable")
        .append(dummyResponse)
        .append(errorHandling)
        .finish());
  }

  private Statement generateRequestCallback() {
    Statement requestCallback = Stmt
        .newObject(RequestCallback.class)
        .extend()
        .publicOverridesMethod("onError", Parameter.of(Request.class, "request"),
            Parameter.of(Throwable.class, "throwable"))
        .append(dummyResponse)
        .append(errorHandling)
        .finish()
        .publicOverridesMethod("onResponseReceived", Parameter.of(Request.class, "request"),
            Parameter.of(Response.class, "response"))
        .append(Stmt.if_(Bool.and(
                Bool.greaterThanOrEqual(Stmt.loadVariable("response").invoke("getStatusCode"), 200),
                Bool.lessThan(Stmt.loadVariable("response").invoke("getStatusCode"), 300)))
            .append(responseHandling)
            .finish()
            .else_()
            .append(Stmt.declareVariable("throwable", ResponseException.class,
                 Stmt.newObject(ResponseException.class).withParameters(
                     Stmt.loadVariable("response").invoke("getStatusText"), Variable.get("response"))))
            .append(errorHandlingWithResponse)
            .finish())
        .finish()
        .finish();

    return requestCallback;
  }
  
  private void generateReturnStatement(BlockBuilder<?> methodBlock, MetaMethod method) {
    Statement returnStatement;
    if (!method.getReturnType().equals(MetaClassFactory.get(void.class))) {
      if (MetaClassFactory.get(Number.class).isAssignableFrom(method.getReturnType().asBoxed())) {
        returnStatement = Stmt.load(0).returnValue();
      } 
      else if (MetaClassFactory.get(Boolean.class).isAssignableFrom(method.getReturnType().asBoxed())) {
        returnStatement = Stmt.load(true).returnValue(); 
      }
      else {
        returnStatement = Stmt.load(null).returnValue();
      }
      methodBlock.append(returnStatement);
    }
    methodBlock.finish();
  }
}