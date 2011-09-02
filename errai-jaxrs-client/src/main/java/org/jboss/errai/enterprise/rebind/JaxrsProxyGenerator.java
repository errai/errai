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

import javax.ws.rs.Path;

import org.jboss.errai.bus.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.bus.client.framework.RPCStub;
import org.jboss.errai.ioc.rebind.ioc.codegen.DefParameters;
import org.jboss.errai.ioc.rebind.ioc.codegen.Parameter;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.BlockBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.ContextualStatementBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;
import org.jboss.errai.ioc.rebind.ioc.codegen.util.Stmt;
import org.jboss.resteasy.specimpl.UriBuilderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates a JAX-RS remote proxy.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class JaxrsProxyGenerator {
  private Logger log = LoggerFactory.getLogger(JaxrsProxyGenerator.class);
  
  private Class<?> remote = null;
  private String rootResourcePath;
  
  public JaxrsProxyGenerator(Class<?> remote) {
    this.remote = remote;
    
    rootResourcePath = MetaClassFactory.get(remote).getAnnotation(Path.class).value();
    if (!rootResourcePath.startsWith("/"))
      rootResourcePath = "/" + rootResourcePath;
  }

  public ClassStructureBuilder<?> generate() {
    ClassStructureBuilder<?> classBuilder = ClassBuilder.define(remote.getSimpleName() + "Impl")
        .packageScope()
        .implementsInterface(remote)
        .implementsInterface(RPCStub.class)
        .body()
        .privateField("remoteCallback", RemoteCallback.class)
        .finish()
        .privateField("errorCallback", ErrorCallback.class)
        .finish();

    classBuilder.publicMethod(void.class, "setErrorCallback", Parameter.of(ErrorCallback.class, "callback"))
        .append(Stmt.loadClassMember("errorCallback").assignValue(Variable.get("callback")))
        .finish();

    classBuilder.publicMethod(void.class, "setRemoteCallback", Parameter.of(RemoteCallback.class, "callback"))
        .append(Stmt.loadClassMember("remoteCallback").assignValue(Variable.get("callback")))
        .finish();

    for (MetaMethod method : MetaClassFactory.get(remote).getMethods()) {
      generateProxyMethod(classBuilder, method);
    }

    return classBuilder;
  }

  private void generateProxyMethod(ClassStructureBuilder<?> classBuilder, MetaMethod method) {
    JaxrsResourceMethod resourceMethod = new JaxrsResourceMethod(method);
   
    BlockBuilder<?> methodBlock = 
      classBuilder.publicMethod(method.getReturnType(), method.getName(),
            DefParameters.from(method).getParameters().toArray(new Parameter[0]));
    
    buildPath(methodBlock, resourceMethod);
    methodBlock.append(Stmt.load(null).returnValue()).finish();
  }
  
  private void buildPath(BlockBuilder<?> methodBlock, JaxrsResourceMethod resourceMethod) {
    String path = rootResourcePath + resourceMethod.getPath();
    List<String> pathParams =
        ((UriBuilderImpl) UriBuilderImpl.fromTemplate(path)).getPathParamNamesInDeclarationOrder();

    ContextualStatementBuilder pathValue = Stmt.loadLiteral(path);
    for (String pathParam : pathParams) {
      pathValue = pathValue.invoke("replaceFirst", "/{" + pathParam + "}/",
          Variable.get(resourceMethod.getPathParameter(pathParam)));
    }

    ContextualStatementBuilder pathBuilder = null;
    if (!resourceMethod.getQueryParameters().isEmpty())
      pathBuilder=Stmt.loadVariable("path").invoke("append", "?");
    
    int i = 0;
    for (String queryParamKey : resourceMethod.getQueryParameters().keySet()) {
      for (String queryParam : resourceMethod.getQueryParameters().get(queryParamKey)) {
        pathBuilder=pathBuilder.invoke("append", queryParamKey);
        pathBuilder=pathBuilder.invoke("append",  "=");
        pathBuilder=pathBuilder.invoke("append", Variable.get(queryParam));
        if(++i < resourceMethod.getNumberOfQueryParams())
          pathBuilder=pathBuilder.invoke("append", "&");
      }
    }
    
    methodBlock.append(Stmt.declareVariable("path", StringBuilder.class, Stmt.newObject(StringBuilder.class)
        .withParameters(pathValue)));
    
    if (pathBuilder!=null)
      methodBlock.append(pathBuilder);
  }
}