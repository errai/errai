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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.jboss.errai.codegen.Cast;
import org.jboss.errai.codegen.Context;
import org.jboss.errai.codegen.DefParameters;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.util.Stmt;

/**
 * Represents parameters of a JAX-RS resource method.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class JaxrsResourceMethodParameters {
  // path param examples that are matched by this regex: /{isbn}/aaa{param}bbb/{name}-{zip}/aaa{param:b+}/{many:.*}
  // leading and trailing white spaces are tolerated
  private static final Pattern PATH_PARAM_PATTERN =
      Pattern.compile("(\\{\\s*)(\\w[\\w.-]*)(:\\s*([^{}][^{}]*))*(\\s*\\})");

  private Statement entityParameter;
  private Map<Class<? extends Annotation>, Map<String, List<Statement>>> parameters;

  public static JaxrsResourceMethodParameters fromMethod(MetaMethod method) {
    List<Parameter> defParams = DefParameters.from(method).getParameters();
    return fromMethod(method, defParams);
  }
  
  public static JaxrsResourceMethodParameters fromMethod(MetaMethod method, String parameterArrayVarName) {
    List<Statement> params = new ArrayList<Statement>();
    Parameter[] defParms = DefParameters.from(method).getParameters().toArray(new Parameter[0]);
    for (int i = 0; i < defParms.length; i++) {
      final MetaClass type = defParms[i].getType();
      final Statement s = Cast.to(type, Stmt.loadVariable(parameterArrayVarName, i));
      params.add(new Statement() {
        @Override
        public String generate(Context context) {
          return s.generate(context);
        }

        @Override
        public MetaClass getType() {
          return type;
        }
      });
    }
    
    return fromMethod(method, params);
  }
  
  public static JaxrsResourceMethodParameters fromMethod(MetaMethod method, List<? extends Statement> parameterValues) {
    JaxrsResourceMethodParameters params = new JaxrsResourceMethodParameters();
    int i = 0;
    for (MetaParameter param : method.getParameters()) {

      Statement paramValue = parameterValues.get(i++);
      Annotation a = param.getAnnotation(PathParam.class);
      if (a != null) {
        params.add(PathParam.class, ((PathParam) a).value(), paramValue);
      }
      else if ((a = param.getAnnotation(QueryParam.class)) != null) {
        params.add(QueryParam.class, ((QueryParam) a).value(), paramValue);
      }
      else if ((a = param.getAnnotation(HeaderParam.class)) != null) {
        params.add(HeaderParam.class, ((HeaderParam) a).value(), paramValue);
      }
      else if ((a = param.getAnnotation(MatrixParam.class)) != null) {
        params.add(MatrixParam.class, ((MatrixParam) a).value(), paramValue);
      }
      else if ((a = param.getAnnotation(FormParam.class)) != null) {
        params.add(FormParam.class, ((FormParam) a).value(), paramValue);
      }
      else if ((a = param.getAnnotation(CookieParam.class)) != null) {
        params.add(CookieParam.class, ((CookieParam) a).value(), paramValue);
      }
      else {
        params.setEntityParameter(paramValue, method);
      }
    }
    return params;

  }

  private void add(Class<? extends Annotation> type, String name, Statement value) {
    if (parameters == null)
      parameters = new HashMap<Class<? extends Annotation>, Map<String, List<Statement>>>();

    Map<String, List<Statement>> params = parameters.get(type);
    if (params == null) {
      parameters.put(type, params = new HashMap<String, List<Statement>>());
    }

    List<Statement> values = params.get(name);
    if (values == null) {
      params.put(name, values = new ArrayList<Statement>());
    }

    values.add(value);
  }

  public Map<String, List<Statement>> getPathParameters() {
    return parameters.get(PathParam.class);
  }

  public Statement getPathParameter(String name) {
    Statement param = getParameterByName(PathParam.class, name);
    if (param == null)
      throw new RuntimeException("No @PathParam found with name:" + name);

    return param;
  }

  public Map<String, List<Statement>> getQueryParameters() {
    return get(QueryParam.class);
  }

  public List<Statement> getQueryParameters(String name) {
    return getQueryParameters().get(name);
  }

  public Map<String, List<Statement>> getHeaderParameters() {
    return get(HeaderParam.class);
  }

  public List<Statement> getHeaderParameters(String name) {
    return getHeaderParameters().get(name);
  }

  public Map<String, List<Statement>> getMatrixParameters() {
    return get(MatrixParam.class);
  }

  public Statement getMatrixParameter(String name) {
    return getParameterByName(MatrixParam.class, name);
  }

  public Map<String, List<Statement>> getFormParameters() {
    return get(FormParam.class);
  }

  public Map<String, List<Statement>> getCookieParameters() {
    return get(CookieParam.class);
  }

  public Statement getCookieParameter(String name) {
    return getParameterByName(CookieParam.class, name);
  }

  public Statement getEntityParameter() {
    return entityParameter;
  }

  private void setEntityParameter(Statement entityParameter, MetaMethod method) {
    if (this.entityParameter != null) {
      throw new RuntimeException("Only one non-annotated entity parameter allowed per method:" + method.getName());
    }
    this.entityParameter = entityParameter;
  }

  private Map<String, List<Statement>> get(Class<? extends Annotation> type) {
    if (parameters == null)
      return null;

    return parameters.get(type);
  }

  private Statement getParameterByName(Class<? extends Annotation> type, String name) {
    Statement param = null;

    if (get(type) != null) {
      List<Statement> params = get(type).get(name);
      if (params != null && !params.isEmpty()) {
        param = params.get(0);
      }
    }

    return param;
  }

  public static List<String> getPathParameterNames(String path) {
    List<String> pathParamNames = new ArrayList<String>();
    Matcher matcher = PATH_PARAM_PATTERN.matcher(path);

    while (matcher.find()) {
      String pathName = matcher.group(2);
      if (pathName != null)
        pathParamNames.add(pathName);
    }

    return pathParamNames;
  }
}