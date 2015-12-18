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

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;

import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.util.Stmt;

import com.google.gwt.http.client.RequestBuilder;

/**
 * Utility to map a JAX-RS {@link HttpMethod} to the corresponding GWT RequestBuilder method.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class JaxrsGwtRequestMethodMapper {

  @SuppressWarnings("serial")
  private static final Map<Class<? extends Annotation>, Statement> METHOD_MAP =
      new HashMap<Class<? extends Annotation>, Statement>() {
        {
          put(GET.class, Stmt.loadStatic(RequestBuilder.class, "GET"));
          put(PUT.class, Stmt.loadStatic(RequestBuilder.class, "PUT"));
          put(POST.class, Stmt.loadStatic(RequestBuilder.class, "POST"));
          put(DELETE.class, Stmt.loadStatic(RequestBuilder.class, "DELETE"));
          put(HEAD.class, Stmt.loadStatic(RequestBuilder.class, "HEAD"));
        }
      };

  /**
   * Searches for {@link HttpMethod} annotations on the provided method and
   * returns the corresponding GWT RequestBuilder method. 
   * 
   * @param method
   * @return statement representing the GWT RequestBuilder method
   */
  public static Statement fromMethod(MetaMethod method) {
    Statement gwtRequestMethod = null;
    for (Class<? extends Annotation> jaxrsMethod : METHOD_MAP.keySet()) {
      if (method.isAnnotationPresent(jaxrsMethod)) {
        gwtRequestMethod = METHOD_MAP.get(jaxrsMethod);
        break;
      }
    }
    return gwtRequestMethod;
  }
}
