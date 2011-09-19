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
  private String entityParameterName;
  private Map<Class<? extends Annotation>, Map<String, List<String>>> parameters;
  
  private JaxrsResourceMethodParameters() {}

  public static JaxrsResourceMethodParameters fromMethod(MetaMethod method) {
    JaxrsResourceMethodParameters parms = new JaxrsResourceMethodParameters();

    List<Parameter> defParams = DefParameters.from(method).getParameters();
    int i = 0;
    for (MetaParameter param : method.getParameters()) {

      String parmName = defParams.get(i++).getName();
      Annotation a = param.getAnnotation(PathParam.class);
      if (a != null) {
        parms.add(PathParam.class, ((PathParam) a).value(), parmName);
      }
      else if ((a = param.getAnnotation(QueryParam.class)) != null) {
        parms.add(QueryParam.class, ((QueryParam) a).value(), parmName);
      }
      else if ((a = param.getAnnotation(HeaderParam.class)) != null) {
        parms.add(HeaderParam.class, ((HeaderParam) a).value(), parmName);
      }
      else if ((a = param.getAnnotation(MatrixParam.class)) != null) {
        parms.add(MatrixParam.class, ((MatrixParam) a).value(), parmName);
      }
      else if ((a = param.getAnnotation(FormParam.class)) != null) {
        parms.add(FormParam.class, ((FormParam) a).value(), parmName);
      }
      else if ((a = param.getAnnotation(CookieParam.class)) != null) {
        parms.add(CookieParam.class, ((CookieParam) a).value(), parmName);
      }
      else {
        parms.setEntityParameterName(parmName, method);
      }
    }
    return parms;
  }

  private void add(Class<? extends Annotation> type, String name, String value) {
    if (parameters == null)
      parameters = new HashMap<Class<? extends Annotation>, Map<String, List<String>>>();
    
    Map<String, List<String>> parms = parameters.get(type);
    if (parms == null) {
      parameters.put(type, parms = new HashMap<String, List<String>>());
    }

    List<String> values = parms.get(name);
    if (values == null) {
      parms.put(name, values = new ArrayList<String>());
    }

    values.add(value);
  }

  public Map<String, List<String>> getPathParameters() {
    return parameters.get(PathParam.class);
  }

  public String getPathParameter(String name, int i) {
    String param = null;

    if (getPathParameters() != null) {
      List<String> params = getPathParameters().get(name);
      if (params != null) {
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

  public Map<String, List<String>> getQueryParameters() {
    return get(QueryParam.class);
  }

  public List<String> getQueryParameters(String name) {
    return getQueryParameters().get(name);
  }

  public Map<String, List<String>> getHeaderParameters() {
    return get(HeaderParam.class);
  }

  public List<String> getHeaderParameters(String name) {
    return getHeaderParameters().get(name);
  }

  public Map<String, List<String>> getMatrixParameters() {
    return get(MatrixParam.class);
  }

  public Map<String, List<String>> getFormParameters() {
    return get(FormParam.class);
  }

  public Map<String, List<String>> getCookieParameters() {
    return get(CookieParam.class);
  }

  private Map<String, List<String>> get(Class<? extends Annotation> type) {
    if (parameters == null)
      return null;
    
    return parameters.get(type);
  }
  
  public String getEntityParameterName() {
    return entityParameterName;
  }

  private void setEntityParameterName(String entityParameterName, MetaMethod method) {
    if (this.entityParameterName != null) {
      throw new RuntimeException("Only one non-annotated entity parameter allowed per method:" + method.getName());
    }
    this.entityParameterName = entityParameterName;
  }
}