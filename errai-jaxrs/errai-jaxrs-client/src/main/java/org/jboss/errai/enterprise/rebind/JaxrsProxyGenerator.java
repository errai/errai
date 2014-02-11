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

import java.util.Collection;

import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.Variable;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.ContextualStatementBuilder;
import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.util.ProxyUtil;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.config.util.ClassScanner;
import org.jboss.errai.enterprise.client.jaxrs.AbstractJaxrsProxy;
import org.jboss.errai.enterprise.client.jaxrs.ClientExceptionMapper;
import org.jboss.errai.enterprise.shared.api.annotations.MapsFrom;

import com.google.gwt.core.ext.GeneratorContext;

/**
 * Generates a JAX-RS remote proxy.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class JaxrsProxyGenerator {
  private MetaClass remote = null;

  private final JaxrsHeaders headers;
  private final String rootResourcePath;
  private final GeneratorContext context;

  public JaxrsProxyGenerator(MetaClass remote, final GeneratorContext context) {
    this.remote = remote;
    this.context = context;
    this.rootResourcePath = remote.getAnnotation(Path.class).value();
    this.headers = JaxrsHeaders.fromClass(remote);
  }

  public ClassStructureBuilder<?> generate() {
    String safeProxyClassName = remote.getFullyQualifiedName().replace('.', '_') + "Impl";
    ClassStructureBuilder<?> classBuilder =
        ClassBuilder.define(safeProxyClassName, AbstractJaxrsProxy.class)
            .packageScope()
            .implementsInterface(remote)
            .body()
            .privateField("remoteCallback", RemoteCallback.class)
            .finish()
            .privateField("errorCallback", ErrorCallback.class)
            .finish()
            .publicConstructor()
            .append(generateCtor())
            .finish()
            .publicMethod(RemoteCallback.class, "getRemoteCallback")
            .append(Stmt.loadClassMember("remoteCallback").returnValue())
            .finish()
            .publicMethod(void.class, "setRemoteCallback", Parameter.of(RemoteCallback.class, "callback"))
            .append(Stmt.loadClassMember("remoteCallback").assignValue(Variable.get("callback")))
            .finish()
            .publicMethod(ErrorCallback.class, "getErrorCallback")
            .append(Stmt.loadClassMember("errorCallback").returnValue())
            .finish()
            .publicMethod(void.class, "setErrorCallback", Parameter.of(ErrorCallback.class, "callback"))
            .append(Stmt.loadClassMember("errorCallback").assignValue(Variable.get("callback")))
            .finish();

    for (MetaMethod method : remote.getMethods()) {
      if (ProxyUtil.shouldProxyMethod(method)) {
        JaxrsResourceMethod resourceMethod = new JaxrsResourceMethod(method, headers, rootResourcePath);
        new JaxrsProxyMethodGenerator(classBuilder, resourceMethod, context).generate();
      }
    }
    return classBuilder;
  }

  /**
   * @return the generated body of the proxy's constructor
   */
  private Statement generateCtor() {
    // Try to find an ClientExceptionMapper that applies to the remote REST interface
    Collection<MetaClass> providers = ClassScanner.getTypesAnnotatedWith(Provider.class, 
        RebindUtils.findTranslatablePackages(context), context);
    MetaClass exceptionMapperClass = null;
    for (MetaClass metaClass : providers) {
      if (!metaClass.isAbstract() && metaClass.isAssignableTo(ClientExceptionMapper.class)) {
        MapsFrom mapsFrom = metaClass.getAnnotation(MapsFrom.class);
        if (mapsFrom == null && exceptionMapperClass == null) {
          // Default mapper
          exceptionMapperClass = metaClass;
        } else {
          Class<?>[] classes = mapsFrom.value();
          if (classes != null) {
            for (Class<?> class1 : classes) {
              if (class1.getName().equals(this.remote.getFullyQualifiedName())) {
                exceptionMapperClass = metaClass;
                break;
              }
            }
          }
        }
      }
    }
    
    // If we found one, create it in the c'tor and assign it to the proxy's exceptionMapper field
    if (exceptionMapperClass != null) {
      ContextualStatementBuilder setMapper = Stmt.loadVariable("this").invoke("setExceptionMapper", 
          Stmt.newObject(exceptionMapperClass));
      return setMapper;
    } else {
      return Stmt.returnVoid();
    }
  }
}