/*
 * Copyright 2011 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.marshalling.rebind;

import static org.jboss.errai.common.rebind.EnvUtil.getEnvironmentConfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.impl.java.JavaReflectionClass;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.common.metadata.MetaDataScanner;
import org.jboss.errai.common.metadata.ScannerSingleton;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.annotations.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.annotations.ImplementationAliases;
import org.jboss.errai.marshalling.client.api.exceptions.InvalidMappingException;
import org.jboss.errai.marshalling.rebind.api.CustomMapping;
import org.jboss.errai.marshalling.rebind.api.InheritedMappings;
import org.jboss.errai.marshalling.rebind.api.impl.defaultjava.DefaultJavaDefinitionMapper;
import org.jboss.errai.marshalling.rebind.api.model.ConstructorMapping;
import org.jboss.errai.marshalling.rebind.api.model.InstantiationMapping;
import org.jboss.errai.marshalling.rebind.api.model.Mapping;
import org.jboss.errai.marshalling.rebind.api.model.MappingDefinition;
import org.jboss.errai.marshalling.rebind.api.model.MemberMapping;
import org.jboss.errai.marshalling.rebind.api.model.impl.SimpleConstructorMapping;
import org.jboss.errai.marshalling.server.marshallers.DefaultDefinitionMarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * The default implementation of {@link DefinitionsFactory}. This implementation covers the detection and mapping of
 * classes annotated with the {@link Portable} annotation, and custom mappings annotated with {@link CustomMapping}.
 * 
 * @author Mike Brock
 */
public class DefinitionsFactoryImpl implements DefinitionsFactory {
  private final Set<Class<?>> exposedClasses = new HashSet<Class<?>>();

  /**
   * Map of aliases to the mapped marshalling type.
   */
  private final Map<String, String> mappingAliases = new HashMap<String, String>();

  private final Map<String, MappingDefinition> MAPPING_DEFINITIONS = new HashMap<String, MappingDefinition>();

  private final Logger log = LoggerFactory.getLogger(MarshallerGeneratorFactory.class);
  
  // key = all types, value = list of all types which inherit from.
  private final Multimap<String, String> inheritanceMap = HashMultimap.create();


  DefinitionsFactoryImpl() {
    loadCustomMappings();
  }

  @Override
  public boolean hasDefinition(String clazz) {
    return MAPPING_DEFINITIONS.containsKey(clazz);
  }

  @Override
  public MappingDefinition getDefinition(String clazz) {
    return MAPPING_DEFINITIONS.get(clazz);
  }

  @Override
  public boolean hasDefinition(MetaClass clazz) {
    return hasDefinition(clazz.getFullyQualifiedName());
  }

  @Override
  public boolean hasDefinition(Class<?> clazz) {
    return hasDefinition(clazz.getName());
  }

  @Override
  public void addDefinition(MappingDefinition definition) {
    putDefinitionIfAbsent(definition.getMappingClass().getFullyQualifiedName(), definition);

    if (definition.getMappingClass().isArray() && definition.getMappingClass().getOuterComponentType().isPrimitive()) {
      putDefinitionIfAbsent(definition.getMappingClass().getInternalName(), definition);
    }

    if (definition.getMappingClass().isPrimitiveWrapper()) {
      putDefinitionIfAbsent(definition.getMappingClass().asUnboxed().getInternalName(), definition);
      putDefinitionIfAbsent(definition.getMappingClass().asUnboxed().getFullyQualifiedName(), definition);
    }

    if (log.isDebugEnabled())
      log.debug("loaded definition: " + definition.getMappingClass().getFullyQualifiedName());
  }

  private void putDefinitionIfAbsent(String key, MappingDefinition value) {
    if (MAPPING_DEFINITIONS.containsKey(key)) {
      throw new IllegalStateException(
          "Mapping definition collision for " + key +
              "\nAlready have: " + MAPPING_DEFINITIONS.get(key) +
              "\nAttempted to add: " + value);
    }
    MAPPING_DEFINITIONS.put(key, value);
  }

  @Override
  public MappingDefinition getDefinition(MetaClass clazz) {
    MappingDefinition def = getDefinition(clazz.getFullyQualifiedName());
    if (def == null) {
      def = getDefinition(clazz.getInternalName());
    }
    return def;
  }

  @Override
  public MappingDefinition getDefinition(Class<?> clazz) {
    return getDefinition(clazz.getName());
  }

  private void loadCustomMappings() {
    MetaDataScanner scanner = ScannerSingleton.getOrCreateInstance();

    for (Class<?> cls : scanner.getTypesAnnotatedWith(CustomMapping.class)) {
      if (!MappingDefinition.class.isAssignableFrom(cls)) {
        throw new RuntimeException("@CustomMapping class: " + cls.getName() + " does not inherit "
            + MappingDefinition.class.getName());
      }

      try {
        MappingDefinition definition = (MappingDefinition) cls.newInstance();
        definition.setMarshallerInstance(new DefaultDefinitionMarshaller(definition));
        addDefinition(definition);
        exposedClasses.add(definition.getMappingClass().asClass());

        if (log.isDebugEnabled())
          log.debug("loaded custom mapping class: " + cls.getName() + " (for mapping: "
                  + definition.getMappingClass().getFullyQualifiedName() + ")");

        if (cls.isAnnotationPresent(InheritedMappings.class)) {
          InheritedMappings inheritedMappings = cls.getAnnotation(InheritedMappings.class);

          for (Class<?> c : inheritedMappings.value()) {
            MappingDefinition aliasMappingDef = new MappingDefinition(c, definition.alreadyGenerated());
            aliasMappingDef.setMarshallerInstance(new DefaultDefinitionMarshaller(aliasMappingDef));
            addDefinition(aliasMappingDef);

            exposedClasses.add(c);

            if (log.isDebugEnabled())
              log.debug("mapping inherited mapping " + c.getName() + " -> " + cls.getName());

          }
        }

      }
      catch (Throwable t) {
        throw new RuntimeException("Failed to load definition", t);
      }
    }

    for (MappingDefinition def : MAPPING_DEFINITIONS.values()) {
      mergeDefinition(def);
    }

    Set<Class<?>> marshallers = scanner.getTypesAnnotatedWith(ClientMarshaller.class);

    for (Class<?> marshallerCls : marshallers) {
      if (Marshaller.class.isAssignableFrom(marshallerCls)) {
        try {
          Class<?> type = (Class<?>) Marshaller.class.getMethod("getTypeHandled").invoke(marshallerCls.newInstance());
          MappingDefinition marshallMappingDef = new MappingDefinition(type, true);
          marshallMappingDef.setClientMarshallerClass(marshallerCls.asSubclass(Marshaller.class));
          addDefinition(marshallMappingDef);

          exposedClasses.add(type);

          if (marshallerCls.isAnnotationPresent(ImplementationAliases.class)) {
            for (Class<?> aliasCls : marshallerCls.getAnnotation(ImplementationAliases.class).value()) {
              MappingDefinition aliasMappingDef = new MappingDefinition(aliasCls, true);
              aliasMappingDef.setClientMarshallerClass(marshallerCls.asSubclass(Marshaller.class));
              addDefinition(aliasMappingDef);

              exposedClasses.add(aliasCls);
              mappingAliases.put(aliasCls.getName(), type.getName());
            }
          }
        }
        catch (Throwable t) {
          throw new RuntimeException("could not instantiate marshaller class: " + marshallerCls.getName(), t);
        }
      }
      else {
        throw new RuntimeException("class annotated with " + ClientMarshaller.class.getCanonicalName()
                + " does not implement " + Marshaller.class.getName());
      }
    }

    exposedClasses.add(Object.class);
    exposedClasses.addAll(getEnvironmentConfig().getExposedClasses());
    mappingAliases.putAll(getEnvironmentConfig().getMappingAliases());

    final Map<Class<?>, Class<?>> aliasToMarshaller = new HashMap<Class<?>, Class<?>>();

    for (Class<?> mappedClass : exposedClasses) {
      if (mappedClass.isSynthetic())
        continue;

      Portable portable = mappedClass.getAnnotation(Portable.class);
      if (portable != null && !portable.aliasOf().equals(Object.class)) {
        aliasToMarshaller.put(mappedClass, portable.aliasOf());
      }
      else if (!hasDefinition(mappedClass)) {
        MappingDefinition def = DefaultJavaDefinitionMapper.map(JavaReflectionClass.newUncachedInstance(mappedClass),
                this);
        def.setMarshallerInstance(new DefaultDefinitionMarshaller(def));
        addDefinition(def);
      }
    }

    for (Map.Entry<Class<?>, Class<?>> entry : aliasToMarshaller.entrySet()) {
      MappingDefinition def = getDefinition(entry.getValue());
      if (def == null) {
        throw new InvalidMappingException("cannot alias type " + entry.getKey().getName()
                + " to " + entry.getValue().getName() + ": the specified alias type does not exist ");
      }

      MappingDefinition aliasDef = new MappingDefinition(def.getMarshallerInstance(), entry.getKey(), false);
      aliasDef.setClientMarshallerClass(def.getClientMarshallerClass());
      mergeDefinition(aliasDef);
      addDefinition(aliasDef);
    }

    for (Map.Entry<String, MappingDefinition> entry : MAPPING_DEFINITIONS.entrySet()) {
      fillInheritanceMap(entry.getValue().getMappingClass());
    }

    MetaClass javaLangObjectRef = MetaClassFactory.get(Object.class);

    for (Map.Entry<String, MappingDefinition> entry : MAPPING_DEFINITIONS.entrySet()) {
      MappingDefinition def = entry.getValue();

      InstantiationMapping instantiationMapping = def.getInstantiationMapping();
      for (Mapping mapping : instantiationMapping.getMappings()) {
        if (shouldUseObjectMarshaller(mapping.getType())) {
          mapping.setType(javaLangObjectRef);
        }
      }

      for (Mapping mapping : entry.getValue().getMemberMappings()) {
        if (shouldUseObjectMarshaller(mapping.getType())) {
          mapping.setType(javaLangObjectRef);
        }
      }
    }

    log.debug("comprehended " + exposedClasses.size() + " classes");
  }

  @Override
  public boolean shouldUseObjectMarshaller(MetaClass type) {
    boolean hasPortableSubtypes = inheritanceMap.containsKey(type.getFullyQualifiedName());
    boolean hasMarshaller = getDefinition(type.asClass()) != null;
    boolean isConcrete = !(type.isAbstract() || type.isInterface());
    return (hasPortableSubtypes && !hasMarshaller) || (hasPortableSubtypes && hasMarshaller && isConcrete);
  }

  /**
   * Populates the inheritance map with all supertypes (except java.lang.Object) and all directly- and
   * indirectly-implemented interfaces of the given class.
   * 
   * @param inheritanceMap
   * @param mappingClass
   */
  private void fillInheritanceMap(MetaClass mappingClass) {
    fillInheritanceMap(inheritanceMap, mappingClass, mappingClass);
  }

  /** Recursive subroutine of {@link #fillInheritanceMap(Multimap, MetaClass)}. */
  private static void fillInheritanceMap(Multimap<String, String> inheritanceMap, MetaClass visiting,
      MetaClass mappingClass) {
    if (visiting == null || visiting.equals(MetaClassFactory.get(Object.class)))
      return;

    if (!visiting.equals(mappingClass)) {
      inheritanceMap.put(visiting.getFullyQualifiedName(), mappingClass.getFullyQualifiedName());
    }

    fillInheritanceMap(inheritanceMap, visiting.getSuperClass(), mappingClass);

    for (MetaClass iface : visiting.getInterfaces()) {
      fillInheritanceMap(inheritanceMap, iface, mappingClass);
    }
  }

  @Override
  public void mergeDefinition(final MappingDefinition def) {
    MetaClass cls = def.getMappingClass();

    while ((cls = cls.getSuperClass()) != null) {
      if (hasDefinition(cls)) {
        MappingDefinition toMerge = getDefinition(cls);

        Set<String> parentKeys = new HashSet<String>();

        for (Mapping m : toMerge.getInstantiationMapping().getMappings())
          parentKeys.add(m.getKey());

        for (MemberMapping m : toMerge.getMemberMappings())
          parentKeys.add(m.getKey());

        Iterator<MemberMapping> defMappings = def.getMemberMappings().iterator();
        while (defMappings.hasNext()) {
          if (parentKeys.contains(defMappings.next().getKey()))
            defMappings.remove();
        }

        for (MemberMapping memberMapping : toMerge.getMemberMappings()) {
          def.addInheritedMapping(memberMapping);
        }

        InstantiationMapping instantiationMapping = def.getInstantiationMapping();

        if (instantiationMapping instanceof ConstructorMapping &&
                def.getInstantiationMapping().getMappings().length == 0 &&
                def.getMappingClass().getDeclaredConstructor(toMerge.getInstantiationMapping().getSignature()) != null) {

          final ConstructorMapping parentConstructorMapping = (ConstructorMapping) toMerge.getInstantiationMapping();
          final MetaClass mergingClass = def.getMappingClass();

          if (parentConstructorMapping instanceof SimpleConstructorMapping) {
            ConstructorMapping newMapping = ((SimpleConstructorMapping) parentConstructorMapping)
                    .getCopyForInheritance();
            newMapping.setMappingClass(mergingClass);
            def.setInheritedInstantiationMapping(newMapping);
          }
        }

        if (log.isDebugEnabled())
          log.debug("merged definition " + def.getMappingClass() + " with " + cls.getFullyQualifiedName());
      }
    }
  }

  @Override
  public boolean isExposedClass(Class<?> clazz) {
    return exposedClasses.contains(clazz);
  }

  @Override
  public Set<Class<?>> getExposedClasses() {
    return Collections.unmodifiableSet(exposedClasses);
  }

  @Override
  public Map<String, String> getMappingAliases() {
    return mappingAliases;
  }

  @Override
  public void deleteAllDefinitions() {
    this.exposedClasses.clear();
    this.mappingAliases.clear();
    this.MAPPING_DEFINITIONS.clear();
  }
}
