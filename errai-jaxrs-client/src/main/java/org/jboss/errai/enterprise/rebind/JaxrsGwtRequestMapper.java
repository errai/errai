package org.jboss.errai.enterprise.rebind;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;

import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;
import org.jboss.errai.ioc.rebind.ioc.codegen.util.Stmt;

import com.google.gwt.http.client.RequestBuilder;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class JaxrsGwtRequestMapper {

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

  public static Statement getGwtRequestMethod(MetaMethod method) {
    Statement gwtRequestMethod = null;
    for (Class<? extends Annotation> jaxrsMethod : map.keySet()) {
      if (method.isAnnotationPresent(jaxrsMethod)) {
        gwtRequestMethod = map.get(jaxrsMethod);
      }
    }
    return gwtRequestMethod;
  }
}
