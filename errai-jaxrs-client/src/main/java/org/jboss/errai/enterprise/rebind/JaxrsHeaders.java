package org.jboss.errai.enterprise.rebind;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

import org.apache.commons.lang.StringUtils;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaMethod;

/**
 * Represents HTTP headers based on JAX-RS annotations.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class JaxrsHeaders {

  private Map<String, String> headers;
  
  private JaxrsHeaders() {};

  public JaxrsHeaders(JaxrsHeaders headers) {
    this.headers.putAll(headers.get());
  }

  /**
   * Generates HTTP headers based on the JAX-RS annotations on the provided class or interface.
   * 
   * @param clazz  the JAX-RS resource class
   * @return headers
   */
  public static JaxrsHeaders fromClass(MetaClass clazz) {
    JaxrsHeaders headers = new JaxrsHeaders();

    Produces p = clazz.getAnnotation(Produces.class);
    if (p != null) {
      headers.setAcceptHeader(p.value());
    }

    Consumes c = clazz.getAnnotation(Consumes.class);
    if (c != null) {
      headers.setContentTypeHeader(c.value());
    }

    return headers;
  }

  /**
   * Generates HTTP headers based on the JAX-RS annotations on the provided method.
   * 
   * @param clazz  the JAX-RS resource class
   * @return headers
   */
  public static JaxrsHeaders fromMethod(MetaMethod method) {
    JaxrsHeaders headers = new JaxrsHeaders();

    Produces p = method.getAnnotation(Produces.class);
    if (p != null) {
      headers.setAcceptHeader(p.value());
    }

    Consumes c = method.getAnnotation(Consumes.class);
    if (c != null) {
      headers.setContentTypeHeader(c.value());
    }

    return headers;
  }

  private void setAcceptHeader(String[] value) {
    if (headers == null) 
      headers = new HashMap<String, String>();
    
    headers.put("Accept", StringUtils.join(value, ","));
  }

  private void setContentTypeHeader(String[] value) {
    if (headers == null) 
      headers = new HashMap<String, String>();

    headers.put("Content-Type", StringUtils.join(value, ","));
  }
  
  public Map<String, String> get() {
    if (headers == null)
      return Collections.<String, String>emptyMap();
    
    return Collections.<String, String>unmodifiableMap(headers);
  }
}