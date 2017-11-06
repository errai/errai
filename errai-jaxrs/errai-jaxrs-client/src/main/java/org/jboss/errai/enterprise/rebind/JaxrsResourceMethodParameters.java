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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;

import org.jboss.errai.codegen.Cast;
import org.jboss.errai.codegen.Context;
import org.jboss.errai.codegen.DefParameters;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.meta.MetaAnnotation;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.enterprise.client.jaxrs.api.MultivaluedMapImpl;
import org.jboss.errai.enterprise.client.jaxrs.api.PathSegmentImpl;

/**
 * Represents parameters of a JAX-RS resource method.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class JaxrsResourceMethodParameters {
  // path param examples which are matched by this regex: /{isbn}/aaa{param}bbb/{name}-{zip}/aaa{param:b+}/{many:.*}
  // leading and trailing white spaces are tolerated
  // see http://docs.jboss.org/resteasy/docs/2.3.4.Final/userguide/html_single/index.html#_PathParam
  private static final Pattern PATH_PARAM_PATTERN =
      Pattern.compile("(\\{\\s*)(\\w[\\w.-]*)(:\\s*([^{}][^{}]*))*(\\s*\\})");

  private Statement entityParameter;
  private Map<Class<? extends Annotation>, MultivaluedMap<String, Statement>> parameters;

  public static JaxrsResourceMethodParameters fromMethod(MetaMethod method) {
    final List<Parameter> defParams = DefParameters.from(method).getParameters();
    return fromMethod(method, defParams);
  }

  public static JaxrsResourceMethodParameters fromMethod(MetaMethod method, String parameterArrayVarName) {
    final List<Statement> params = new ArrayList<Statement>();
    final Parameter[] defParms = DefParameters.from(method).getParameters().toArray(new Parameter[0]);
    for (int i = 0; i < defParms.length; i++) {
      final MetaClass type = defParms[i].getType().asBoxed();
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
    final JaxrsResourceMethodParameters params = new JaxrsResourceMethodParameters();
    int i = 0;

    for (final MetaParameter param : method.getParameters()) {

      final Statement paramValue = parameterValues.get(i++);
      Optional<MetaAnnotation> a = param.getAnnotation(PathParam.class);
      if (a.isPresent()) {
        params.add(PathParam.class, a.get().value(), paramValue);
      }
      else if ((a = param.getAnnotation(QueryParam.class)).isPresent()) {
        params.add(QueryParam.class, a.get().value(), paramValue);
      }
      else if ((a = param.getAnnotation(HeaderParam.class)).isPresent()) {
        params.add(HeaderParam.class, a.get().value(), paramValue);
      }
      else if ((a = param.getAnnotation(MatrixParam.class)).isPresent()) {
        params.add(MatrixParam.class, a.get().value(), paramValue);
      }
      else if ((a = param.getAnnotation(FormParam.class)).isPresent()) {
        params.add(FormParam.class, a.get().value(), paramValue);
      }
      else if ((a = param.getAnnotation(CookieParam.class)).isPresent()) {
        params.add(CookieParam.class, a.get().value(), paramValue);
      }
      else {
        params.setEntityParameter(paramValue, method);
      }
    }
    return params;

  }

  private void add(Class<? extends Annotation> type, String name, Statement value) {
    if (parameters == null) {
      parameters = new HashMap<>();
    }

    MultivaluedMap<String, Statement> params = parameters.get(type);
    if (params == null) {
      parameters.put(type, params = new MultivaluedMapImpl<>());
    }
    params.add(name, value);
  }

  public MultivaluedMap<String, Statement> getPathParameters() {
    return parameters.get(PathParam.class);
  }

  public Statement getPathParameter(String name) {
    final Statement param = getParameterByName(PathParam.class, name);
    if (param == null)
      throw new RuntimeException("No @PathParam found with name:" + name);

    if (MetaClassFactory.get(PathSegment.class).equals(param.getType())) {
      return new Statement() {
        @Override
        public String generate(Context context) {
          if (param instanceof Parameter) {
            return Stmt.castTo(PathSegmentImpl.class, Stmt.loadVariable(((Parameter) param).getName()))
              .invoke("getEncodedPathWithParameters").generate(context);
          }
          else {
            return Stmt.castTo(PathSegmentImpl.class, param).invoke("getEncodedPathWithParameters").generate(context);
          }
        }

        @Override
        public MetaClass getType() {
          return MetaClassFactory.get(String.class);
        }
      };
    }

    return param;
  }

  public MultivaluedMap<String, Statement> getQueryParameters() {
    return get(QueryParam.class);
  }

  public List<Statement> getQueryParameters(String name) {
    return getQueryParameters().get(name);
  }

  public MultivaluedMap<String, Statement> getHeaderParameters() {
    return get(HeaderParam.class);
  }

  public List<Statement> getHeaderParameters(String name) {
    return getHeaderParameters().get(name);
  }

  public MultivaluedMap<String, Statement> getMatrixParameters() {
    return get(MatrixParam.class);
  }

  public Statement getMatrixParameter(String name) {
    return getParameterByName(MatrixParam.class, name);
  }

  public MultivaluedMap<String, Statement> getFormParameters() {
    return get(FormParam.class);
  }

  public MultivaluedMap<String, Statement> getCookieParameters() {
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

  private MultivaluedMap<String, Statement> get(Class<? extends Annotation> type) {
    if (parameters == null)
      return null;

    return parameters.get(type);
  }

  private Statement getParameterByName(Class<? extends Annotation> type, String name) {
    Statement param = null;

    if (get(type) != null) {
      final List<Statement> params = get(type).get(name);
      if (params != null && !params.isEmpty()) {
        param = params.get(0);
      }
    }

    return param;
  }

  public static List<String> getPathParameterExpressions(String path) {
    final String pathWithNestedBracesRemoved = replaceNestedCurlyBraces(path);
    final List<String> pathParamNames = new ArrayList<String>();
    final Matcher matcher = PATH_PARAM_PATTERN.matcher(pathWithNestedBracesRemoved);
    
    while (matcher.find()) {
      final String id = matcher.group(2);
      final String regex = matcher.group(3);
      if (id != null) {
        final String pathParamExpr = recoverNestedCurlyBraces(id + ((regex != null) ? regex : ""));
        pathParamNames.add(pathParamExpr);
      }
    }

    return pathParamNames;
  }

  public boolean needsEncoding(String paramName) {
    // PathSegments are encoded in PathSegmentImpl
    if ((MetaClassFactory.get(PathSegment.class).equals(getParameterByName(PathParam.class, paramName).getType()))) {
      return false;
    }
      
    return true;
  }
  
  private static final char openCurlyReplacement = 6;
  private static final char closeCurlyReplacement = 7;
  
  private static String replaceNestedCurlyBraces(String str) {
    final char[] chars = str.toCharArray();
    int open = 0;
    for (int i = 0; i < chars.length; i++) {
      if (chars[i] == '{') {
        if (open != 0)
          chars[i] = openCurlyReplacement;
        open++;
      }
      else if (chars[i] == '}') {
        open--;
        if (open != 0) {
          chars[i] = closeCurlyReplacement;
        }
      }
    }
    return new String(chars);
  }

  private static String recoverNestedCurlyBraces(String str) {
    return str.replace(openCurlyReplacement, '{').replace(closeCurlyReplacement, '}');
  }

}
