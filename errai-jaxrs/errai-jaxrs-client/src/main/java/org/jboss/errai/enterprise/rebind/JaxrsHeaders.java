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

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

import org.apache.commons.lang3.StringUtils;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaMethod;

/**
 * Represents HTTP headers based on JAX-RS annotations.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class JaxrsHeaders {

  private Map<String, String> headers;
  
  private JaxrsHeaders() {};

  public JaxrsHeaders(JaxrsHeaders headers) {
    this.headers.putAll(headers.get());
  }

  /**
   * Generates HTTP headers based on the JAX-RS annotations on the provided class or interface.
   * 
   * @param clazz  the JAX-RS resource class
   * @return headers
   */
  public static JaxrsHeaders fromClass(MetaClass clazz) {
    JaxrsHeaders headers = new JaxrsHeaders();

    Produces p = clazz.getAnnotation(Produces.class);
    if (p != null) {
      headers.setAcceptHeader(p.value());
    }

    Consumes c = clazz.getAnnotation(Consumes.class);
    if (c != null) {
      headers.setContentTypeHeader(c.value());
    }

    return headers;
  }

  /**
   * Generates HTTP headers based on the JAX-RS annotations on the provided method.
   * 
   * @param clazz  the JAX-RS resource class
   * @return headers
   */
  public static JaxrsHeaders fromMethod(MetaMethod method) {
    JaxrsHeaders headers = new JaxrsHeaders();

    Produces p = method.getAnnotation(Produces.class);
    if (p != null) {
      headers.setAcceptHeader(p.value());
    }

    Consumes c = method.getAnnotation(Consumes.class);
    if (c != null) {
      headers.setContentTypeHeader(c.value());
    }

    return headers;
  }

  private void setAcceptHeader(String[] value) {
    if (value == null)
      return;
    
    if (headers == null) 
      headers = new HashMap<String, String>();
    
    headers.put("Accept", StringUtils.join(value, ","));
  }

  private void setContentTypeHeader(String[] value) {
    if (value == null)
      return;
        
    if (headers == null) 
      headers = new HashMap<String, String>();
    
    if (value.length == 1)
      headers.put("Content-Type", value[0]);
  }
  
  public Map<String, String> get() {
    if (headers == null)
      return Collections.<String, String>emptyMap();
    
    return Collections.<String, String>unmodifiableMap(headers);
  }
}
