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

package org.jboss.errai.enterprise.client.jaxrs.api;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;

import com.google.gwt.http.client.URL;

/**
 * GWT-translatable implementation of {@link PathSegment}.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class PathSegmentImpl implements PathSegment {
  private final MultivaluedMap<String, String> matrixParameters = new MultivaluedMapImpl<String, String>();
  private final String path;

  public PathSegmentImpl(String segment) {
    this.path = segment.contains(";") ? segment.substring(0, segment.indexOf(";")) : segment;

    String[] matrixParams = segment.split(";");
    if (matrixParams.length > 1) {
      for (String matrixParam : matrixParams) {
        String[] param = matrixParam.split("=");
        if (param != null && param.length > 0) {
          String name = param[0];
          String value = "";
          if (param.length > 1) {
            value = param[1];
          }
          matrixParameters.add(name, value);
        }
      }
    }
  }

  @Override
  public String getPath() {
    return path;
  }
  
  @Override
  public MultivaluedMap<String, String> getMatrixParameters() {
    return matrixParameters;
  }

  public String getEncodedPathWithParameters() {
    StringBuilder builder = new StringBuilder(URL.encodePathSegment(path));
    for (String matrixParamName : matrixParameters.keySet()) {
      builder
        .append(";" + matrixParamName + "=")
        .append(URL.encodePathSegment(matrixParameters.getFirst(matrixParamName)));
    }
    
    return builder.toString();
  }
  
  @Override
  public String toString() {
    return getEncodedPathWithParameters();
  }  
}