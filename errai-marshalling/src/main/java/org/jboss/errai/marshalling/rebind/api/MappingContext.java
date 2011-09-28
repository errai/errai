package org.jboss.errai.marshalling.rebind.api;

import org.jboss.errai.codegen.framework.Context;
import org.jboss.errai.marshalling.client.api.Marshaller;

import java.util.*;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class MappingContext {
  private Map<String, Class<? extends Marshaller>> registeredMarshallers
          = new HashMap<String, Class<? extends Marshaller>>();
  private List<String> renderedMarshallers = new ArrayList<String>(); 
  

  private Context codegenContext;

  public MappingContext(Context codegenContext) {
    this.codegenContext = codegenContext;
  }

  public Class<? extends Marshaller> getMarshaller(Class<?> clazz) {
    return getMarshaller(clazz.getName());
  }
  
  public Class<? extends Marshaller> getMarshaller(String clazzName) {
    return registeredMarshallers.get(clazzName);
  }
  
  public void registerMarshaller(String clazzName, Class<? extends Marshaller> clazz) {
    registeredMarshallers.put(clazzName, clazz);
  }
  
  public boolean hasMarshaller(Class<?> clazz) {
    return hasMarshaller(clazz.getName());
  }
  
  public boolean hasMarshaller(String clazzName) {
    return registeredMarshallers.containsKey(clazzName);
  }

  public Map<String, Class<? extends Marshaller>> getAllMarshallers() {
    return Collections.unmodifiableMap(registeredMarshallers);
  }

  public Context getCodegenContext() {
    return codegenContext;
  }
  
  public void markRendered(Class<?> clazz) {
    markRendered(clazz.getName());
  }
  
  public void markRendered(String className) {
    renderedMarshallers.add(className);
  }
}
