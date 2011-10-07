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
  private ArrayMarshallerCallback arrayMarshallerCallback;

  public MappingContext(Context codegenContext, MetaClass generatedBootstrapClass,
                        ClassStructureBuilder<?> classStructureBuilder,
                        ArrayMarshallerCallback callback) {

    this.codegenContext = codegenContext;
    this.generatedBootstrapClass = generatedBootstrapClass;
    this.classStructureBuilder = classStructureBuilder;
    this.arrayMarshallerCallback = callback;
  }

  public Class<? extends Marshaller> getMarshaller(MetaClass clazz) {
    if (clazz.isArray()) {
      clazz = clazz.getOuterComponentType();
    }

    return getMarshaller(clazz.getFullyQualifiedName());
  }

  private Class<? extends Marshaller> getMarshaller(String clazzName) {
    return registeredMarshallers.get(clazzName);
  }

  public void registerGeneratedMarshaller(String clazzName) {
    generatedMarshallers.add(clazzName);
  }

  public void registerMarshaller(String clazzName, Class<? extends Marshaller> clazz) {
    registeredMarshallers.put(clazzName, clazz);
  }

  public boolean hasMarshaller(MetaClass clazz) {
    if (clazz.isArray()) {
      clazz = clazz.getOuterComponentType();
    }

    return hasMarshaller(clazz.getFullyQualifiedName());
  }

  public boolean hasMarshaller(String clazzName) {
    return registeredMarshallers.containsKey(clazzName);
  }

  public boolean hasGeneratedMarshaller(MetaClass clazz) {
    if (clazz.isArray()) {
      clazz = clazz.getOuterComponentType();
    }

    return hasGeneratedMarshaller(clazz.getFullyQualifiedName());
  }

  private boolean hasGeneratedMarshaller(String clazzName) {
    return generatedMarshallers.contains(clazzName);
  }

  public boolean hasProvidedOrGeneratedMarshaller(MetaClass clazz) {
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

  public ArrayMarshallerCallback getArrayMarshallerCallback() {
    return arrayMarshallerCallback;
  }
}
