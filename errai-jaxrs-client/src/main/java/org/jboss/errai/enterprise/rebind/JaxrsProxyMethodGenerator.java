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

import java.util.List;

import org.jboss.errai.codegen.framework.DefParameters;
import org.jboss.errai.codegen.framework.Parameter;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.Variable;
import org.jboss.errai.codegen.framework.builder.BlockBuilder;
import org.jboss.errai.codegen.framework.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.framework.builder.ContextualStatementBuilder;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.meta.MetaMethod;
import org.jboss.errai.codegen.framework.util.Bool;
import org.jboss.errai.codegen.framework.util.Stmt;
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

  private Statement errorHandling;
  private Statement responseHandling;

  public JaxrsProxyMethodGenerator(JaxrsResourceMethod resourceMethod) {
    this.resourceMethod = resourceMethod;
  }

  public void generate(ClassStructureBuilder<?> classBuilder) {
    this.errorHandling = Stmt.loadStatic(classBuilder.getClassDefinition(), "this")
        .invoke("handleError", Variable.get("throwable"));
    this.responseHandling = Stmt.loadStatic(classBuilder.getClassDefinition(), "this")
        .invoke("handleResponse", Variable.get("response"));

    MetaMethod method = resourceMethod.getMethod();

    BlockBuilder<?> methodBlock =
        classBuilder.publicMethod(method.getReturnType(), method.getName(),
            DefParameters.from(method).getParameters().toArray(new Parameter[0]));

    if (resourceMethod.getHttpMethod() != null) {
      generateUrl(methodBlock);
      generateRequestBuilder(methodBlock);
      generateHeaders(methodBlock);
      generateRequest(methodBlock);
    }

    if (!method.getReturnType().equals(MetaClassFactory.get(void.class)))
      methodBlock.append(Stmt.load(null).returnValue());

    methodBlock.finish();
  }

  private void generateUrl(BlockBuilder<?> methodBlock) {
    JaxrsResourceMethodParameters parms = resourceMethod.getParameters();
    ContextualStatementBuilder pathValue = Stmt.loadLiteral(resourceMethod.getPath());

    List<String> pathParams =
        ((UriBuilderImpl) UriBuilderImpl.fromTemplate(resourceMethod.getPath())).getPathParamNamesInDeclarationOrder();
    int i = 0;
    for (String pathParam : pathParams) {
      pathValue = pathValue.invoke("replaceFirst", "\\{" + pathParam + "\\}",
          Variable.get(parms.getPathParameter(pathParam, i++)));
    }

    methodBlock.append(Stmt.declareVariable("url", StringBuilder.class,
        Stmt.newObject(StringBuilder.class).withParameters(pathValue)));

    ContextualStatementBuilder urlBuilder = null;
    if (parms.getQueryParameters() != null) {
      urlBuilder = Stmt.loadVariable("url").invoke(APPEND, "?");

      i = 0;
      for (String queryParamName : parms.getQueryParameters().keySet()) {
        for (String queryParam : parms.getQueryParameters(queryParamName)) {
          if (i++ > 0)
            urlBuilder = urlBuilder.invoke(APPEND, "&");

          urlBuilder = urlBuilder.invoke(APPEND, queryParamName);
          urlBuilder = urlBuilder.invoke(APPEND, "=");
          urlBuilder = urlBuilder.invoke(APPEND, Variable.get(queryParam));
        }
      }
    }

    if (urlBuilder != null)
      methodBlock.append(urlBuilder);
  }

  private void generateHeaders(BlockBuilder<?> methodBlock) {
    JaxrsResourceMethodParameters parms = resourceMethod.getParameters();

    for (String key : resourceMethod.getHeaders().keySet()) {
      methodBlock.append(Stmt.loadVariable("requestBuilder").invoke("setHeader", key, 
          resourceMethod.getHeaders().get(key)));
    }
    
    if (parms.getHeaderParameters() != null) {
      for (String headerParamName : parms.getHeaderParameters().keySet()) {
        ContextualStatementBuilder headerValueBuilder = Stmt.nestedCall(Stmt.newObject(StringBuilder.class));
        
        int i = 0;
        for (String headerParam : parms.getHeaderParameters(headerParamName)) {
          if (i++ > 0) {
            headerValueBuilder = headerValueBuilder.invoke(APPEND, ",");
          }
          headerValueBuilder = headerValueBuilder.invoke(APPEND, Variable.get(headerParam));
        }

        methodBlock.append(Stmt.loadVariable("requestBuilder").invoke("setHeader", headerParamName, 
            headerValueBuilder.invoke("toString")));
      }
    }
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
    if (resourceMethod.getParameters().getEntityParameterName() == null) {
      sendRequest = sendRequest.invoke("sendRequest", null, generateRequestCallback());
    }
    else {
      // TODO serialization
      Statement body = Variable.get(resourceMethod.getParameters().getEntityParameterName());
      sendRequest = sendRequest.invoke("sendRequest", body, generateRequestCallback());
    }

    methodBlock.append(Stmt
        .try_()
        .append(sendRequest)
        .finish()
        .catch_(RequestException.class, "throwable")
        // TODO separate error callback for JAX-RS rpcs?
        .append(errorHandling)
        .finish());
  }

  private Statement generateRequestCallback() {
    Statement requestCallback = Stmt
        .newObject(RequestCallback.class)
        .extend()
        .publicOverridesMethod("onError", Parameter.of(Request.class, "request"),
            Parameter.of(Throwable.class, "throwable"))
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
            .append(Stmt.declareVariable("throwable", RequestException.class,
                 Stmt.newObject(RequestException.class).withParameters(
                     Stmt.invokeStatic(Integer.class, "toString",
                         Stmt.loadVariable("response").invoke("getStatusCode")))))
            .append(errorHandling)
            .finish())
        .finish()
        .finish();

    return requestCallback;
  }
}