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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Path;

import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.meta.MetaMethod;

/**
 * Represents a JAX-RS resource method.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class JaxrsResourceMethod {
  private MetaMethod method;
  private Statement httpMethod;
  private String path;

  private JaxrsResourceMethodParameters parameters;
  private JaxrsHeaders resourceClassHeaders;
  private JaxrsHeaders methodHeaders;

  public JaxrsResourceMethod(MetaMethod method, JaxrsHeaders headers, String rootResourcePath) {
    this.method = method;

    Path subResourcePath = method.getAnnotation(Path.class);
    String fullPath = "/" + rootResourcePath + ((subResourcePath != null) ? subResourcePath.value() : "");
    this.path = fullPath.replaceAll("//", "/").replaceFirst("/", "");
    
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
}