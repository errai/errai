package org.jboss.errai.enterprise.rebind;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Path;

import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;

/**
 * Represents a JAX-RS resource method.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class JaxrsResourceMethod {
  private MetaMethod method;
  private Statement httpMethod;
  private String path;

  private JaxrsResourceMethodParameters parameters;
  private JaxrsHeaders resourceClassHeaders;
  private JaxrsHeaders methodHeaders;

  public JaxrsResourceMethod(MetaMethod method, JaxrsHeaders headers, String rootResourcePath) {
    this.method = method;

    Path subResourcePath = method.getAnnotation(Path.class);
    this.path = rootResourcePath + ((subResourcePath != null) ? subResourcePath.value() : "");
    this.httpMethod = JaxrsGwtRequestMethodMapper.fromMethod(method);
    this.parameters = JaxrsResourceMethodParameters.fromMethod(method);
    this.methodHeaders = JaxrsHeaders.fromMethod(method);
    this.resourceClassHeaders = headers;
  }

  public MetaMethod getMethod() {
    return method;
  }

  public Statement getHttpMethod() {
    return httpMethod;
  }

  public String getPath() {
    return path;
  }

  public JaxrsResourceMethodParameters getParameters() {
    return parameters;
  }
  
  public Map<String, String> getHeaders() {
    Map<String, String> headers = new HashMap<String, String>();
   
    if (resourceClassHeaders.get() != null)
      headers.putAll(resourceClassHeaders.get());
    
    if (methodHeaders.get() != null)
      headers.putAll(methodHeaders.get());
    
    return Collections.unmodifiableMap(headers);
  }
}