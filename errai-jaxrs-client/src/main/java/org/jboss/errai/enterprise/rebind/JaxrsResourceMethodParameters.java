package org.jboss.errai.enterprise.rebind;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.jboss.errai.codegen.framework.DefParameters;
import org.jboss.errai.codegen.framework.Parameter;
import org.jboss.errai.codegen.framework.meta.MetaMethod;
import org.jboss.errai.codegen.framework.meta.MetaParameter;

/**
 * Represents parameters of a JAX-RS resource method.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class JaxrsResourceMethodParameters {
  private Parameter entityParameter;
  private Map<Class<? extends Annotation>, Map<String, List<Parameter>>> parameters;
  
  private JaxrsResourceMethodParameters() {}

  public static JaxrsResourceMethodParameters fromMethod(MetaMethod method) {
    JaxrsResourceMethodParameters parms = new JaxrsResourceMethodParameters();

    List<Parameter> defParams = DefParameters.from(method).getParameters();
    int i = 0;
    for (MetaParameter param : method.getParameters()) {

      Parameter defParam = defParams.get(i++);
      Annotation a = param.getAnnotation(PathParam.class);
      if (a != null) {
        parms.add(PathParam.class, ((PathParam) a).value(), defParam);
      }
      else if ((a = param.getAnnotation(QueryParam.class)) != null) {
        parms.add(QueryParam.class, ((QueryParam) a).value(), defParam);
      }
      else if ((a = param.getAnnotation(HeaderParam.class)) != null) {
        parms.add(HeaderParam.class, ((HeaderParam) a).value(), defParam);
      }
      else if ((a = param.getAnnotation(MatrixParam.class)) != null) {
        parms.add(MatrixParam.class, ((MatrixParam) a).value(), defParam);
      }
      else if ((a = param.getAnnotation(FormParam.class)) != null) {
        parms.add(FormParam.class, ((FormParam) a).value(), defParam);
      }
      else if ((a = param.getAnnotation(CookieParam.class)) != null) {
        parms.add(CookieParam.class, ((CookieParam) a).value(), defParam);
      }
      else {
        parms.setEntityParameter(defParam, method);
      }
    }
    return parms;
  }

  private void add(Class<? extends Annotation> type, String name, Parameter value) {
    if (parameters == null)
      parameters = new HashMap<Class<? extends Annotation>, Map<String, List<Parameter>>>();
    
    Map<String, List<Parameter>> parms = parameters.get(type);
    if (parms == null) {
      parameters.put(type, parms = new HashMap<String, List<Parameter>>());
    }

    List<Parameter> values = parms.get(name);
    if (values == null) {
      parms.put(name, values = new ArrayList<Parameter>());
    }

    values.add(value);
  }

  public Map<String, List<Parameter>> getPathParameters() {
    return parameters.get(PathParam.class);
  }

  public Parameter getPathParameter(String name, int i) {
    Parameter param = null;

    if (getPathParameters() != null) {
      List<Parameter> params = getPathParameters().get(name);
      if (params != null && params.size() > i) {
        do {
          param = params.get(i--);
        }
        while (i >= 0);
      }
    }

    if (param == null)
      throw new RuntimeException("No @PathParam found with name:" + name);

    return param;
  }

  public Map<String, List<Parameter>> getQueryParameters() {
    return get(QueryParam.class);
  }

  public List<Parameter> getQueryParameters(String name) {
    return getQueryParameters().get(name);
  }

  public Map<String, List<Parameter>> getHeaderParameters() {
    return get(HeaderParam.class);
  }

  public List<Parameter> getHeaderParameters(String name) {
    return getHeaderParameters().get(name);
  }

  public Map<String, List<Parameter>> getMatrixParameters() {
    return get(MatrixParam.class);
  }

  public Map<String, List<Parameter>> getFormParameters() {
    return get(FormParam.class);
  }

  public Map<String, List<Parameter>> getCookieParameters() {
    return get(CookieParam.class);
  }

  private Map<String, List<Parameter>> get(Class<? extends Annotation> type) {
    if (parameters == null)
      return null;
    
    return parameters.get(type);
  }
  
  public Parameter getEntityParameter() {
    return entityParameter;
  }

  private void setEntityParameter(Parameter entityParameter, MetaMethod method) {
    if (this.entityParameter != null) {
      throw new RuntimeException("Only one non-annotated entity parameter allowed per method:" + method.getName());
    }
    this.entityParameter = entityParameter;
  }
}