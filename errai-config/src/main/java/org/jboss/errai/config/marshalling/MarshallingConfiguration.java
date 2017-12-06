/*
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.config.marshalling;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.common.client.api.annotations.NonPortable;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.common.client.types.TypeHandler;
import org.jboss.errai.common.client.types.TypeHandlerFactory;
import org.jboss.errai.config.ErraiConfiguration;
import org.jboss.errai.config.MetaClassFinder;
import org.jboss.errai.config.propertiesfile.ErraiAppPropertiesConfiguration;
import org.jboss.errai.config.rebind.EnvironmentConfigExtension;
import org.jboss.errai.config.rebind.ExposedTypesProvider;
import org.jboss.errai.config.util.ClassScanner;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class MarshallingConfiguration {

  private static final Map<MetaClass, MetaClass> TYPE_HANDLERS_INHERITANCE_MAP = TypeHandlerFactory.inheritanceMap()
          .entrySet()
          .stream()
          .collect(toMap(e -> MetaClassFactory.get(e.getKey()), e -> MetaClassFactory.get(e.getValue())));

  private static final Map<MetaClass, Map<MetaClass, TypeHandler>> TYPE_HANDLERS = TypeHandlerFactory.handlers()
          .entrySet()
          .stream()
          .collect(toMap(e -> MetaClassFactory.get(e.getKey()), s -> s.getValue()
                  .entrySet()
                  .stream()
                  .collect(toMap(x -> MetaClassFactory.get(x.getKey()), Map.Entry::getValue))));

  private static ErraiAppPropertiesConfiguration erraiAppPropertiesConfiguration;

  private static ErraiAppPropertiesConfiguration getErraiAppPropertiesConfiguration() {

    if (erraiAppPropertiesConfiguration == null) {
      erraiAppPropertiesConfiguration = new ErraiAppPropertiesConfiguration();
    }

    return erraiAppPropertiesConfiguration;
  }

  public static Set<MetaClass> allPortableConcreteSubtypes(final ErraiConfiguration erraiConfiguration,
          final MetaClassFinder metaClassFinder,
          final MetaClass metaClass) {

    final Set<MetaClass> allPortableConcreteSubtypes = allPortableTypes(erraiConfiguration, metaClassFinder).stream()
            .filter(s -> !s.isInterface())
            .filter(s -> s.isAssignableTo(metaClass))
            .collect(toSet());

    if (isPortableType(metaClassFinder, erraiConfiguration, metaClass)) {
      allPortableConcreteSubtypes.add(metaClass);
    }

    return allPortableConcreteSubtypes;
  }

  public static boolean isPortableType(final MetaClassFinder metaClassFinder,
          final ErraiConfiguration erraiConfiguration,
          final MetaClass metaClass) {

    return metaClass.isAnnotationPresent(Portable.class) || metaClass.instanceOf(String.class) || isBuiltinPortable(
            metaClass) || allPortableTypes(erraiConfiguration, metaClassFinder).contains(metaClass);
  }

  private static Set<MetaClass> allPortableTypes(final ErraiConfiguration erraiConfiguration,
          final MetaClassFinder metaClassFinder) {

    final HashSet<MetaClass> allPortableTypes = new HashSet<>();
    allPortableTypes.addAll(allExposedPortableTypes(erraiConfiguration, metaClassFinder));
    allPortableTypes.addAll(allNonExposedPortableTypes(erraiConfiguration, metaClassFinder));
    return allPortableTypes;
  }

  public static Set<MetaClass> allExposedPortableTypes(final ErraiConfiguration erraiConfiguration,
          final MetaClassFinder metaClassFinder) {

    final Set<MetaClass> exposedTypes = erraiConfiguration.modules().portableTypes();
    final Set<MetaClass> nonPortableTypes = erraiConfiguration.modules().nonPortableTypes();

    final Set<MetaClass> annotatedNonPortableTypes = metaClassFinder.findAnnotatedWith(NonPortable.class);
    final Set<MetaClass> annotatedPortableTypes = metaClassFinder.findAnnotatedWith(Portable.class);

    nonPortableTypes.addAll(annotatedNonPortableTypes);

    addExposedInnerClasses(exposedTypes, annotatedPortableTypes);
    exposedTypes.addAll(annotatedPortableTypes);

    processEnvironmentConfigExtensions(exposedTypes, metaClassFinder);

    // must do this before filling in interfaces and supertypes!
    exposedTypes.removeAll(nonPortableTypes);

    return exposedTypes.stream().map(MetaClass::getErased).collect(toSet());
  }

  private static Set<MetaClass> allNonExposedPortableTypes(final ErraiConfiguration erraiConfiguration,
          final MetaClassFinder metaClassFinder) {

    final Set<MetaClass> nonExposedPortableTypes = new HashSet<>();

    for (final MetaClass cls : allExposedPortableTypes(erraiConfiguration, metaClassFinder)) {
      fillInInterfacesAndSuperTypes(nonExposedPortableTypes, cls);
    }

    return nonExposedPortableTypes;
  }

  private static boolean isBuiltinPortable(final MetaClass metaClass) {
    if (!TYPE_HANDLERS.containsKey(metaClass) && TYPE_HANDLERS_INHERITANCE_MAP.containsKey(metaClass)) {
      return isBuiltinPortable(TYPE_HANDLERS_INHERITANCE_MAP.get(metaClass));
    } else {
      return TYPE_HANDLERS.get(metaClass) != null;
    }
  }

  private static void processEnvironmentConfigExtensions(final Set<MetaClass> exposedClasses,
          final MetaClassFinder metaClassFinder) {

    final Collection<MetaClass> exts = metaClassFinder.findAnnotatedWith(EnvironmentConfigExtension.class);
    for (final MetaClass cls : exts) {
      try {
        final Class<? extends ExposedTypesProvider> providerClass = Class.forName(cls.getFullyQualifiedName())
                .asSubclass(ExposedTypesProvider.class);

        final Constructor<? extends ExposedTypesProvider> constructor = providerClass.getConstructor(
                MetaClassFinder.class);
        constructor.setAccessible(true);

        for (final MetaClass exposedType : constructor.newInstance(metaClassFinder).provideTypesToExpose()) {
          if (exposedType.isPrimitive()) {
            exposedClasses.add(exposedType.asBoxed());
          } else if (exposedType.isConcrete()) {
            exposedClasses.add(exposedType);
          }
        }
      } catch (final Throwable e) {
        throw new RuntimeException("unable to load environment extension: " + cls.getFullyQualifiedName(), e);
      }
    }
  }

  private static void addExposedInnerClasses(final Set<MetaClass> exposedClasses,
          final Set<MetaClass> exposedFromScanner) {
    for (final MetaClass cls : exposedFromScanner) {
      for (final MetaClass decl : cls.getDeclaredClasses()) {
        if (decl.isSynthetic()) {
          continue;
        }
        exposedClasses.add(decl);
      }
    }
  }

  private static void fillInInterfacesAndSuperTypes(final Set<MetaClass> set, final MetaClass type) {
    for (final MetaClass iface : type.getInterfaces()) {
      set.add(iface);
      fillInInterfacesAndSuperTypes(set, iface);
    }
    if (type.getSuperClass() != null) {
      fillInInterfacesAndSuperTypes(set, type.getSuperClass());
    }
  }
}
