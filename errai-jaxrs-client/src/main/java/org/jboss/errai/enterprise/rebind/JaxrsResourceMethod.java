package org.jboss.errai.enterprise.rebind;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MultivaluedMap;

import org.jboss.errai.ioc.rebind.ioc.codegen.DefParameters;
import org.jboss.errai.ioc.rebind.ioc.codegen.Parameter;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaParameter;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;

import com.google.gwt.http.client.RequestBuilder;

/**
 * Represents a JAX-RS resource method.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class JaxrsResourceMethod {
  private MetaMethod method;
  
  private RequestBuilder.Method httpMethod;
  private String path;
  private MultivaluedMap<String, String> queryParameters = new MultivaluedMapImpl<String, String>();
  private MultivaluedMap<String, String> pathParameters = new MultivaluedMapImpl<String, String>();

  private int numberOfQueryParams = 0;
  // TODO MatrixParam, FormParam, CookieParam

  public JaxrsResourceMethod(MetaMethod method) {
    this.method = method;
    
    Path subResourcePath = method.getAnnotation(Path.class);
    path = (subResourcePath != null) ? subResourcePath.value() : ""; 
    
    parseHttpMethod();
    parseParameters();
  }
  
  private void parseHttpMethod() {
    if (!method.isAnnotationPresent(GET.class)) {
      httpMethod = RequestBuilder.GET;
    } else if (!method.isAnnotationPresent(POST.class)) {
      httpMethod = RequestBuilder.POST;
    } else if (!method.isAnnotationPresent(PUT.class)) {
      httpMethod = RequestBuilder.PUT;
    } else if (!method.isAnnotationPresent(DELETE.class)) {
      httpMethod = RequestBuilder.DELETE;
    }
  }
  
  private void parseParameters() {
    List<Parameter> defParams = DefParameters.from(method).getParameters();
    int i = 0;
    for (MetaParameter param : method.getParameters()) {

      Annotation a = param.getAnnotation(PathParam.class);
      if (a != null) {
        pathParameters.add(((PathParam) a).value(), defParams.get(i).getName());
      }
      else if ((a = param.getAnnotation(QueryParam.class)) != null) {
        numberOfQueryParams++;
        queryParameters.add(((QueryParam) a).value(), defParams.get(i).getName());
      }
      // ...
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
}