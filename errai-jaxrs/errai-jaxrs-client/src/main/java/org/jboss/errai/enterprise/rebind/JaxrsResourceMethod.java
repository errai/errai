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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Path;

import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.meta.MetaMethod;

/**
 * Represents a JAX-RS resource method.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class JaxrsResourceMethod {
  private final MetaMethod method;
  private final Statement httpMethod;
  private final String path;

  private final JaxrsResourceMethodParameters parameters;
  private final JaxrsHeaders resourceClassHeaders;
  private final JaxrsHeaders methodHeaders;

  public JaxrsResourceMethod(MetaMethod method, JaxrsHeaders headers, String rootResourcePath) {
    Path subResourcePath = method.getAnnotation(Path.class);
    String fullPath = rootResourcePath;
    if (fullPath.startsWith("/")) {
      fullPath = fullPath.substring(1);
    }
    
    if (fullPath.endsWith("/")) {
      fullPath = fullPath.substring(0, fullPath.length() - 1); 
    }

    if (subResourcePath != null) {
      if (!subResourcePath.value().startsWith("/")) {
        fullPath += "/";
      }
      fullPath += subResourcePath.value();
    }

    this.method = method;
    this.path = fullPath;
    this.httpMethod = JaxrsGwtRequestMethodMapper.fromMethod(method);
    this.parameters = JaxrsResourceMethodParameters.fromMethod(method);
    this.methodHeaders = JaxrsHeaders.fromMethod(method);
    this.resourceClassHeaders = headers;
  }

  public MetaMethod getMethod() {
    return method;
  }

  public Statement getHttpMethod() {
    return httpMethod;
  }

  public String getPath() {
    return path;
  }

  public JaxrsResourceMethodParameters getParameters() {
    return parameters;
  }
  
  public Map<String, String> getHeaders() {
    Map<String, String> headers = new HashMap<String, String>();
   
    if (resourceClassHeaders.get() != null)
      headers.putAll(resourceClassHeaders.get());
    
    if (methodHeaders.get() != null)
      headers.putAll(methodHeaders.get());
    
    return Collections.unmodifiableMap(headers);
  }
  
  public String getAcceptHeader() {
    return getHeaders().get("Accept");
  }
  
  public String getContentTypeHeader() {
    return getHeaders().get("Content-Type");
  }
}
