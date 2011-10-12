package org.jboss.errai.marshalling.rebind.api;

import org.jboss.errai.codegen.framework.Context;
import org.jboss.errai.codegen.framework.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassMember;
import org.jboss.errai.codegen.framework.meta.MetaField;
import org.jboss.errai.codegen.framework.meta.MetaMethod;
import org.jboss.errai.codegen.framework.util.GenUtil;
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

  private Map<String, String> mappingAliases = new HashMap<String, String>();
  private Map<String, List<String>> reverseMappingAlias = new HashMap<String, List<String>>();

  private Set<String> exposedMembers = new HashSet<String>();

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
    if (mappingAliases.containsKey(clazzName)) {
      clazzName = mappingAliases.get(clazzName);
    }

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

    if (clazz.isPrimitive()) {
      clazz = clazz.asBoxed();
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
    if (clazz.isPrimitive()) {
      clazz = clazz.asBoxed();
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

  private static String getPrivateMemberName(MetaClassMember member) {
    if (member instanceof MetaField) {
      return GenUtil.getPrivateFieldInjectorName((MetaField) member);
    }
    else {
      return GenUtil.getPrivateMethodName((MetaMethod) member);
    }
  }

  public void markExposed(MetaClassMember member) {
    exposedMembers.add(getPrivateMemberName(member));
  }

  public boolean isExposed(MetaClassMember member) {
    return exposedMembers.contains(getPrivateMemberName(member));
  }

  public void registerMappingAlias(Class<?> from, Class<?> to) {
    registerMappingAlias(from.getName(), to.getName());
  }

  public void registerMappingAlias(String from, String to) {
    mappingAliases.put(from, to);

    if (!reverseMappingAlias.containsKey(to)) {
      reverseMappingAlias.put(to, new ArrayList<String>());
    }
    reverseMappingAlias.get(to).add(from);
  }

  public List<String> getReverseMappingAliasFor(String type) {
    if (reverseMappingAlias.containsKey(type)) {
      return reverseMappingAlias.get(type);
    }
    else {
      return Collections.emptyList();
    }
  }
}
