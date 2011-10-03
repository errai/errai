package org.jboss.errai.marshalling.rebind.api;

import org.jboss.errai.codegen.framework.Context;
import org.jboss.errai.codegen.framework.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.marshalling.client.api.Marshaller;

import java.util.*;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class MappingContext {
  private Map<String, Class<? extends Marshaller>> registeredMarshallers
          = new HashMap<String, Class<? extends Marshaller>>();
  private Set<String> generatedMarshallers = new HashSet<String>();
  
  private List<String> renderedMarshallers = new ArrayList<String>(); 
  
  private Context codegenContext;

  private MetaClass generatedBootstrapClass;
  private ClassStructureBuilder<?> classStructureBuilder;

  public MappingContext(Context codegenContext, MetaClass generatedBootstrapClass,
                        ClassStructureBuilder<?> classStructureBuilder) {

    this.codegenContext = codegenContext;
    this.generatedBootstrapClass = generatedBootstrapClass;
    this.classStructureBuilder = classStructureBuilder;
  }

  public Class<? extends Marshaller> getMarshaller(Class<?> clazz) {
    return getMarshaller(clazz.getName());
  }
  
  public Class<? extends Marshaller> getMarshaller(String clazzName) {
    return registeredMarshallers.get(clazzName);
  }
  
  public void registerGeneratedMarshaller(String clazzName) {
    generatedMarshallers.add(clazzName);
  }
  
  public void registerMarshaller(String clazzName, Class<? extends Marshaller> clazz) {
    registeredMarshallers.put(clazzName, clazz);
  }

  public boolean hasMarshaller(MetaClass clazz) {
    return hasMarshaller(clazz.getFullyQualifiedName());
  }

  public boolean hasMarshaller(Class<?> clazz) {
    return hasMarshaller(clazz.getName());
  }
  
  public boolean hasMarshaller(String clazzName) {
    return registeredMarshallers.containsKey(clazzName);
  }

  public boolean hasGeneratedMarshaller(Class<?> clazz) {
    return hasGeneratedMarshaller(clazz.getName());
  }
  
  public boolean hasGeneratedMarshaller(String clazzName) {
    return generatedMarshallers.contains(clazzName);
  }

  public boolean hasProvidedOrGeneratedMarshaller(MetaClass clazz) {
    return hasProvidedOrGeneratedMarshaller(clazz.getFullyQualifiedName());
  }

  public boolean hasProvidedOrGeneratedMarshaller(Class<?> clazz) {
    return hasProvidedOrGeneratedMarshaller(clazz.getName());
  }
  
  public boolean hasProvidedOrGeneratedMarshaller(String clazz) {
    return hasMarshaller(clazz) || hasGeneratedMarshaller(clazz);
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

  public MetaClass getGeneratedBootstrapClass() {
    return generatedBootstrapClass;
  }

  public ClassStructureBuilder<?> getClassStructureBuilder() {
    return classStructureBuilder;
  }
}
