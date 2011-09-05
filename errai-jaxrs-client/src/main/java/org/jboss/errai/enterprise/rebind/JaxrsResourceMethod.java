package org.jboss.errai.enterprise.rebind;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MultivaluedMap;

import org.jboss.errai.ioc.rebind.ioc.codegen.DefParameters;
import org.jboss.errai.ioc.rebind.ioc.codegen.Parameter;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaParameter;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;

/**
 * Represents a JAX-RS resource method.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class JaxrsResourceMethod {
  private MetaMethod method;
  private Statement httpMethod;
  private String path;
  private String entityParameterName;
  
  private MultivaluedMap<String, String> queryParameters = new MultivaluedMapImpl<String, String>();
  private MultivaluedMap<String, String> pathParameters = new MultivaluedMapImpl<String, String>();
  private MultivaluedMap<String, String> matrixParameters = new MultivaluedMapImpl<String, String>();
  private MultivaluedMap<String, String> formParameters = new MultivaluedMapImpl<String, String>();
  private MultivaluedMap<String, String> cookieParameters = new MultivaluedMapImpl<String, String>();
  private MultivaluedMap<String, String> headerParameters = new MultivaluedMapImpl<String, String>();
  
  private int numberOfQueryParams = 0;
  
  public JaxrsResourceMethod(MetaMethod method) {
    this.method = method;
    
    Path subResourcePath = method.getAnnotation(Path.class);
    path = (subResourcePath != null) ? subResourcePath.value() : ""; 
    httpMethod = JaxrsGwtRequestMapper.getGwtRequestMethod(method);

    parseParameters(method);
  }
  
  private void parseParameters(MetaMethod method) {
    List<Parameter> defParams = DefParameters.from(method).getParameters();
    int i = 0;
    for (MetaParameter param : method.getParameters()) {
      
      String parmName = defParams.get(i).getName();
      Annotation a = param.getAnnotation(PathParam.class);
      if (a != null) {
        pathParameters.add(((PathParam) a).value(), parmName);
      }
      else if ((a = param.getAnnotation(QueryParam.class)) != null) {
        numberOfQueryParams++;
        queryParameters.add(((QueryParam) a).value(), parmName);
      } else {
        setEntityParameterName(parmName, method);
      }
      // TODO ...
      i++;
    }
  }

  public String getPathParameter(String name) {
    String param = null;

    List<String> params = pathParameters.get(name);
    if (params != null && !params.isEmpty()) {
      param = params.remove(0);
    }

    if (param == null)
      throw new RuntimeException("No more @PathParam found with name:" + name);

    return param;
  }

  public MultivaluedMap<String, String> getPathParameters() {
    return pathParameters;
  }

  public MultivaluedMap<String, String> getQueryParameters() {
    return queryParameters;
  }
  
  public int getNumberOfQueryParams() {
    return numberOfQueryParams;
  }
  
  public String getPath() {
    return path;
  }
  
  public Statement getHttpMethod() {
    return httpMethod;
  }
  
  public MetaMethod getMethod() {
    return method;
  }
  
  public String getEntityParameterName() {
    return entityParameterName;
  }

  public void setEntityParameterName(String entityParameterName, MetaMethod method) {
    if (this.entityParameterName != null) {
      throw new RuntimeException("Only one non-annotated entity parameter allowed per method:" + method.getName());
    }
    this.entityParameterName = entityParameterName;
  }
}