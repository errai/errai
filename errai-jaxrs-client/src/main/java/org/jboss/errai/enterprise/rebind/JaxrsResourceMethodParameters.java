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

import org.jboss.errai.ioc.rebind.ioc.codegen.DefParameters;
import org.jboss.errai.ioc.rebind.ioc.codegen.Parameter;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaParameter;

/**
 * Represents parameters of a resource method.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class JaxrsResourceMethodParameters extends HashMap<Class<? extends Annotation>, Map<String, List<String>>> {

  private String entityParameterName;
  
  private JaxrsResourceMethodParameters() {}
  
  public static JaxrsResourceMethodParameters fromMethod(MetaMethod method) {
    JaxrsResourceMethodParameters parameters = new JaxrsResourceMethodParameters();

      List<Parameter> defParams = DefParameters.from(method).getParameters();
      int i = 0;
      for (MetaParameter param : method.getParameters()) {

        String parmName = defParams.get(i++).getName();
        Annotation a = param.getAnnotation(PathParam.class);
        if (a != null) {
          parameters.add(PathParam.class, ((PathParam) a).value(), parmName);
        }
        else if ((a = param.getAnnotation(QueryParam.class)) != null) {
          parameters.add(QueryParam.class, ((QueryParam) a).value(), parmName);
        }
        else if ((a = param.getAnnotation(HeaderParam.class)) != null) {
          parameters.add(HeaderParam.class, ((HeaderParam) a).value(), parmName);
        }
        else if ((a = param.getAnnotation(MatrixParam.class)) != null) {
          parameters.add(MatrixParam.class, ((MatrixParam) a).value(), parmName);
        }
        else if ((a = param.getAnnotation(FormParam.class)) != null) {
          parameters.add(FormParam.class, ((FormParam) a).value(), parmName);
        }
        else if ((a = param.getAnnotation(CookieParam.class)) != null) {
          parameters.add(CookieParam.class, ((CookieParam) a).value(), parmName);
        }
        else {
          parameters.setEntityParameterName(parmName, method);
        }
      }
    return parameters;
  }
  
  private void add(Class<? extends Annotation> key, String name, String value) {
    Map<String, List<String>> parameters = get(key);
    if (parameters == null) {
      put(key, parameters = new HashMap<String, List<String>>());
    }

    List<String> values = parameters.get(name);
    if (values == null) {
      parameters.put(name, values = new ArrayList<String>());
    }

    values.add(value);
  }
  
  public Map<String, List<String>> getPathParameters() {
    return get(PathParam.class);
  }
  
  public String getPathParameter(String name, int i) {
    String param = null;

    if (getPathParameters() != null) {
      List<String> params = getPathParameters().get(name);
      if (params!=null) {
        do {
          param = params.get(i--);
        } while (i>=0);
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