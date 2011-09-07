package org.jboss.errai.enterprise.rebind;

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

  public JaxrsResourceMethod(MetaMethod method) {
    this.method = method;

    Path subResourcePath = method.getAnnotation(Path.class);
    path = (subResourcePath != null) ? subResourcePath.value() : "";
    httpMethod = JaxrsGwtRequestMethodMapper.fromMethod(method);
    parameters = JaxrsResourceMethodParameters.fromMethod(method);
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
}