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
import org.jboss.errai.common.client.api.annotations.ExposeEntity;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.common.metadata.MetaDataScanner;
import org.jboss.errai.common.metadata.ScannerSingleton;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.annotations.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.annotations.ImplementationAliases;
import org.jboss.errai.marshalling.rebind.api.CustomMapping;
import org.jboss.errai.marshalling.rebind.api.InheritedMappings;
import org.jboss.errai.marshalling.rebind.api.impl.defaultjava.DefaultJavaDefinitionMapper;
import org.jboss.errai.marshalling.rebind.api.model.Mapping;
import org.jboss.errai.marshalling.rebind.api.model.MappingDefinition;
import org.jboss.errai.marshalling.rebind.api.model.MemberMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
  private final Set<String> exposedClassesStr = new HashSet<String>();

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
    return hasDefinition(clazz.getCanonicalName());
  }

  @Override
  public boolean hasDefinition(Class<?> clazz) {
    return hasDefinition(clazz.getCanonicalName());
  }

  @Override
  public void addDefinition(MappingDefinition definition) {
    MAPPING_DEFINITIONS.put(definition.getMappingClass().getCanonicalName(), definition);
    if (log.isDebugEnabled())
      log.debug("loaded definition: " + definition.getMappingClass().getFullyQualifiedName());
  }

  @Override
  public MappingDefinition getDefinition(MetaClass clazz) {
    return getDefinition(clazz.getCanonicalName());
  }

  @Override
  public MappingDefinition getDefinition(Class<?> clazz) {
    return getDefinition(clazz.getCanonicalName());
  }

  private void loadCustomMappings() {
    MetaDataScanner scanner = ScannerSingleton.getOrCreateInstance();

    for (Class<?> cls : scanner.getTypesAnnotatedWith(CustomMapping.class)) {
      if (!MappingDefinition.class.isAssignableFrom(cls)) {
        throw new RuntimeException("@CustomMapping class: " + cls.getName() + " does not inherit " + MappingDefinition.class.getName());
      }

      try {
        MappingDefinition definition = (MappingDefinition) cls.newInstance();
        addDefinition(definition);
        exposedClassesStr.add(definition.getMappingClass().getFullyQualifiedName());

        if (log.isDebugEnabled())
          log.debug("loaded custom mapping class: " + cls.getName() + " (for mapping: " + definition.getMappingClass().getFullyQualifiedName() + ")");
      }
      catch (Throwable t) {
        throw new RuntimeException("Failed to load definition", t);
      }

      if (cls.isAnnotationPresent(InheritedMappings.class)) {
        InheritedMappings inheritedMappings = cls.getAnnotation(InheritedMappings.class);

        for (Class<?> c : inheritedMappings.value()) {
          addDefinition(new MappingDefinition(c));
          exposedClassesStr.add(c.getName());

          if (log.isDebugEnabled())
            log.debug("mapping inherited mapping " + c.getName() + " -> " + cls.getName());

        }
      }
    }

    for (MappingDefinition def : MAPPING_DEFINITIONS.values()) {
      mergeDefinition(def);
    }

    Set<Class<?>> marshallers = scanner.getTypesAnnotatedWith(ClientMarshaller.class);

    for (Class<?> cls : marshallers) {
      if (Marshaller.class.isAssignableFrom(cls)) {
        try {
          Class<?> type = (Class<?>) Marshaller.class.getMethod("getTypeHandled").invoke(cls.newInstance());
          exposedClassesStr.add(type.getName());

          if (cls.isAnnotationPresent(ImplementationAliases.class)) {
            for (Class<?> c : cls.getAnnotation(ImplementationAliases.class).value()) {
              exposedClassesStr.add(c.getName());
            }
          }
        }
        catch (Throwable t) {
          throw new RuntimeException("could not instantiate marshaller class: " + cls.getName(), t);
        }
      }
      else {
        throw new RuntimeException("class annotated with " + ClientMarshaller.class.getCanonicalName()
                + " does not implement " + Marshaller.class.getName());
      }
    }


    Set<Class<?>> exposedFromScanner = new HashSet<Class<?>>(scanner.getTypesAnnotatedWith(Portable.class));
    exposedFromScanner.addAll(scanner.getTypesAnnotatedWith(ExposeEntity.class));

    // Set<Class<?>> exposedClasses = new HashSet<Class<?>>();

    for (Class<?> cls : exposedFromScanner) {
      for (Class<?> decl : cls.getDeclaredClasses()) {
        if (decl.isEnum()) continue;
        exposedClasses.add(decl);
      }
    }

    exposedClasses.addAll(exposedFromScanner);
    // add all GWT JRE  classes

    exposedClasses.add(Throwable.class);
    exposedClasses.add(NullPointerException.class);
    exposedClasses.add(RuntimeException.class);
    exposedClasses.add(Exception.class);
    exposedClasses.add(ArithmeticException.class);
    exposedClasses.add(ArrayStoreException.class);
    exposedClasses.add(AssertionError.class);
    exposedClasses.add(ClassCastException.class);
    exposedClasses.add(IllegalArgumentException.class);
    exposedClasses.add(IndexOutOfBoundsException.class);
    exposedClasses.add(NegativeArraySizeException.class);
    exposedClasses.add(NumberFormatException.class);
    exposedClasses.add(StringIndexOutOfBoundsException.class);
    exposedClasses.add(UnsupportedOperationException.class);
    exposedClasses.add(StackTraceElement.class);

    exposedClasses.add(IOException.class);
    exposedClasses.add(UnsupportedEncodingException.class);
    exposedClasses.add(ConcurrentModificationException.class);
    exposedClasses.add(EmptyStackException.class);

    for (Class<?> clazz : exposedClasses) {
      exposedClassesStr.add(clazz.getName());
    }

    for (Class<?> mappedClass : exposedClasses) {
      addDefinition(DefaultJavaDefinitionMapper.map(MetaClassFactory.get(mappedClass), this));
    }

    log.info("comprehended " + exposedClasses.size() + " classes");
  }


  @Override
  public void mergeDefinition(MappingDefinition def) {
    MetaClass cls = def.getMappingClass();

    while ((cls = cls.getSuperClass()) != null) {
      if (hasDefinition(cls)) {
        MappingDefinition toMerge = getDefinition(cls);

        Set<String> parentKeys = new HashSet<String>();

        for (Mapping m : toMerge.getConstructorMapping().getMappings())
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

        if (def.getMappingClass().getDeclaredConstructor(toMerge.getConstructorMapping().getConstructorSignature()) != null) {
          def.setConstructorMapping(toMerge.getConstructorMapping());
        }
        
        if (log.isDebugEnabled())
          log.debug("merged definition " + def.getMappingClass() + " with " + cls.getFullyQualifiedName());
      }
    }
  }

  @Override
  public boolean isExposedClass(String clazz) {
    return exposedClassesStr.contains(clazz);
  }

  public Set<Class<?>> getExposedClasses() {
    return Collections.unmodifiableSet(exposedClasses);
  }
}

