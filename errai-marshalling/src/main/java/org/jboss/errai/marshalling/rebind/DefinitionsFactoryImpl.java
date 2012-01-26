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

import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.meta.impl.java.JavaReflectionClass;
import org.jboss.errai.common.client.api.annotations.ExposeEntity;
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
import org.jboss.errai.marshalling.rebind.api.model.*;
import org.jboss.errai.marshalling.rebind.api.model.impl.SimpleConstructorMapping;
import org.jboss.errai.marshalling.rebind.util.MarshallingGenUtil;
import org.jboss.errai.marshalling.server.marshallers.DefaultDefinitionMarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * The default implementation of {@link DefinitionsFactory}. This implementation covers the detection and
 * mapping of classes annotated with the {@link Portable} annotation, and custom mappings annotated with
 * {@link CustomMapping}.
 *
 * @author Mike Brock
 */
public class DefinitionsFactoryImpl implements DefinitionsFactory {
  private final Set<Class<?>> exposedClasses = new HashSet<Class<?>>();

  /**
   * Map of aliases to the mapped marshalling type.
   */
  private final Map<String, String> mappingAliases = new HashMap<String, String>();

  private final Map<String, MappingDefinition> MAPPING_DEFINITIONS
          = new HashMap<String, MappingDefinition>();

  private Logger log = LoggerFactory.getLogger(MarshallerGeneratorFactory.class);


  public DefinitionsFactoryImpl() {
    loadCustomMappings();
  }

  public DefinitionsFactoryImpl(Map<String, MappingDefinition> mappingDefinitions) {
    MAPPING_DEFINITIONS.putAll(mappingDefinitions);
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
    
    MAPPING_DEFINITIONS.put(definition.getMappingClass().getFullyQualifiedName(), definition);
    
    if (definition.getMappingClass().isArray() && definition.getMappingClass().getOuterComponentType().isPrimitive()) {
      MAPPING_DEFINITIONS.put(definition.getMappingClass().getInternalName(), definition);
    }
    
    if (definition.getMappingClass().isPrimitiveWrapper()) {
      MAPPING_DEFINITIONS.put(definition.getMappingClass().asUnboxed().getInternalName(), definition);
    }
    
    if (log.isDebugEnabled())
      log.debug("loaded definition: " + definition.getMappingClass().getFullyQualifiedName());
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
        throw new RuntimeException("@CustomMapping class: " + cls.getName() + " does not inherit " + MappingDefinition.class.getName());
      }

      try {
        MappingDefinition definition = (MappingDefinition) cls.newInstance();
        definition.setMarshallerInstance(new DefaultDefinitionMarshaller(definition));
        addDefinition(definition);
        exposedClasses.add(definition.getMappingClass().asClass());

        if (log.isDebugEnabled())
          log.debug("loaded custom mapping class: " + cls.getName() + " (for mapping: " + definition.getMappingClass().getFullyQualifiedName() + ")");


        if (cls.isAnnotationPresent(InheritedMappings.class)) {
          InheritedMappings inheritedMappings = cls.getAnnotation(InheritedMappings.class);

          for (Class<?> c : inheritedMappings.value()) {
            MappingDefinition aliasMappingDef = new MappingDefinition(c);
            aliasMappingDef.setMarshallerInstance(new DefaultDefinitionMarshaller(aliasMappingDef));
            addDefinition(aliasMappingDef);

            exposedClasses.add(c);
            mappingAliases.put(c.getName(), cls.getName());

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
          MappingDefinition marshallMappingDef = new MappingDefinition(type);
          marshallMappingDef.setClientMarshallerClass(marshallerCls.asSubclass(Marshaller.class));
          addDefinition(marshallMappingDef);

          exposedClasses.add(type);

          if (marshallerCls.isAnnotationPresent(ImplementationAliases.class)) {
            for (Class<?> aliasCls : marshallerCls.getAnnotation(ImplementationAliases.class).value()) {
              MappingDefinition aliasMappingDef = new MappingDefinition(aliasCls);
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

    Set<Class<?>> exposedFromScanner = new HashSet<Class<?>>(scanner.getTypesAnnotatedWith(Portable.class));
    exposedFromScanner.addAll(scanner.getTypesAnnotatedWith(ExposeEntity.class));

    for (Class<?> cls : exposedFromScanner) {
      for (Class<?> decl : cls.getDeclaredClasses()) {
        if (decl.isSynthetic()) {
          continue;
        }

        exposedClasses.add(decl);
      }
    }

    exposedClasses.addAll(exposedFromScanner);

    exposedClasses.add(Object.class);

    Properties props = scanner.getProperties("ErraiApp.properties");
    if (props != null) {
      log.info("Checking ErraiApp.properties for configured types ...");

      for (Object o : props.keySet()) {
        String key = (String) o;
        if (key.equals(MarshallingGenUtil.CONFIG_ERRAI_OLD_SERIALIZABLE_TYPE) || key.equals(MarshallingGenUtil.CONFIG_ERRAI_SERIALIZABLE_TYPE)) {
          for (String s : props.getProperty(key).split(" ")) {
            try {
              Class<?> cls = Class.forName(s.trim());
              exposedClasses.add(cls);
            }
            catch (Exception e) {
              throw new RuntimeException("could not find class defined in ErraiApp.properties for serialization: " + s);
            }
          }

          break;
        }

        if (key.equals(MarshallingGenUtil.CONFIG_ERRAI_MAPPING_ALIASES)) {
          for (String s : props.getProperty(key).split(" ")) {
            try {
              String[] mapping = s.split("->");
              
              if (mapping.length != 2) {
                throw new RuntimeException("syntax error: mapping for marshalling alias: " + s);
              }

              Class<?> fromMapping = Class.forName(mapping[0].trim());
              Class<?> toMapping = Class.forName(mapping[1].trim());

              mappingAliases.put(fromMapping.getName(), toMapping.getName());
            }
            catch (Exception e) {
              throw new RuntimeException("could not find class defined in ErraiApp.properties for mapping: " + s);
            }
          }
          break;
        }
      }
    }


    Map<Class<?>, Class<?>> aliasToMarshaller = new HashMap<Class<?>, Class<?>>();

    for (Class<?> mappedClass : exposedClasses) {
      if (mappedClass.isSynthetic()) continue;

      Portable portable = mappedClass.getAnnotation(Portable.class);
      if (portable != null && !portable.aliasOf().equals(Object.class)) {
        aliasToMarshaller.put(mappedClass, portable.aliasOf());
      }
      else if (!hasDefinition(mappedClass)) {
        MappingDefinition def = DefaultJavaDefinitionMapper.map(JavaReflectionClass.newUncachedInstance(mappedClass), this);
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

      mappingAliases.put(entry.getKey().getName(), def.getClientMarshallerClass().getName());

      MappingDefinition aliasDef = new MappingDefinition(def.getMarshallerInstance(), entry.getKey());
      aliasDef.setClientMarshallerClass(def.getClientMarshallerClass());
      addDefinition(aliasDef);
    }

    // key = all types, value = list of all types which inherit from.
    Map<String, List<String>> inheritanceMap = new HashMap<String, List<String>>();

    for (Map.Entry<String, MappingDefinition> entry : MAPPING_DEFINITIONS.entrySet()) {
      checkInheritance(inheritanceMap, entry.getValue().getMappingClass());
    }

    MetaClass javaLangObjectRef = MetaClassFactory.get(Object.class);

    for (Map.Entry<String, MappingDefinition> entry : MAPPING_DEFINITIONS.entrySet()) {
      MappingDefinition def = entry.getValue();

      InstantiationMapping instantiationMapping = def.getInstantiationMapping();
      for (Mapping mapping : instantiationMapping.getMappings()) {
        if (!isTypeFinal(inheritanceMap, mapping.getType())) {
          mapping.setType(javaLangObjectRef);
        }
      }

      for (Mapping mapping : entry.getValue().getMemberMappings()) {
        if (!isTypeFinal(inheritanceMap, mapping.getType())) {
          mapping.setType(javaLangObjectRef);
        }
      }
    }


    log.info("comprehended " + exposedClasses.size() + " classes");
  }

  private static boolean isTypeFinal(Map<String, List<String>> inheritanceMap, MetaClass type) {
    List<String> subTypes = inheritanceMap.get(type.getFullyQualifiedName());
    return subTypes == null || subTypes.isEmpty();
  }


  private static void checkInheritance(Map<String, List<String>> inheritanceMap, MetaClass root) {
    MetaClass cls = root;
    String fqcn;

    do {
      fqcn = cls.getFullyQualifiedName();

      if (cls.getSuperClass() != null)
        registerInheritance(inheritanceMap, cls.getSuperClass().getFullyQualifiedName(), fqcn);

      for (MetaClass iface : cls.getInterfaces()) {
        checkInheritance(inheritanceMap, iface);
      }

    }
    while ((cls = cls.getSuperClass()) != null && cls.getFullyQualifiedName().equals(Object.class.getName()));
  }

  static void registerInheritance(Map<String, List<String>> inheritanceMap, String parent, String child) {
    List<String> subtypes = inheritanceMap.get(parent);
    if (subtypes == null) {
      subtypes = new ArrayList<String>();
      inheritanceMap.put(parent, subtypes);
    }
    subtypes.add(child);
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
          if (parentKeys.contains(defMappings.next().getKey())) defMappings.remove();
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
            ConstructorMapping newMapping = ((SimpleConstructorMapping) parentConstructorMapping).getCopyForInheritance();
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

  public Set<Class<?>> getExposedClasses() {
    return Collections.unmodifiableSet(exposedClasses);
  }

  @Override
  public Map<String, String> getMappingAliases() {
    return mappingAliases;
  }
}

