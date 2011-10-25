package org.jboss.errai.enterprise.rebind;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;

import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.meta.MetaMethod;
import org.jboss.errai.codegen.framework.util.Stmt;

import com.google.gwt.http.client.RequestBuilder;

/**
 * Utility to map a JAX-RS {@link HttpMethod} to the corresponding GWT RequestBuilder method.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class JaxrsGwtRequestMethodMapper {

  private static Map<Class<? extends Annotation>, Statement> map =
      new HashMap<Class<? extends Annotation>, Statement>() {
        {
          put(GET.class, Stmt.loadStatic(RequestBuilder.class, "GET"));
          put(PUT.class, Stmt.loadStatic(RequestBuilder.class, "PUT"));
          put(POST.class, Stmt.loadStatic(RequestBuilder.class, "POST"));
          put(DELETE.class, Stmt.loadStatic(RequestBuilder.class, "DELETE"));
          put(HEAD.class, Stmt.loadStatic(RequestBuilder.class, "HEAD"));
        }
      };

  /**
   * Searches for {@link HttpMethod} annotations on the provided method and
   * returns the corresponding GWT RequestBuilder method. 
   * 
   * @param method
   * @return statement representing the GWT RequestBuilder method
   */
  public static Statement fromMethod(MetaMethod method) {
    Statement gwtRequestMethod = null;
    for (Class<? extends Annotation> jaxrsMethod : map.keySet()) {
      if (method.isAnnotationPresent(jaxrsMethod)) {
        gwtRequestMethod = map.get(jaxrsMethod);
        break;
      }
    }
    return gwtRequestMethod;
  }
}
