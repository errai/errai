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

package org.jboss.errai.config;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.common.client.types.TypeHandler;
import org.jboss.errai.common.client.types.TypeHandlerFactory;
import org.jboss.errai.config.rebind.EnvironmentConfigExtension;
import org.jboss.errai.config.rebind.ExposedTypesProvider;

import java.util.Collection;
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

  @Deprecated
  public static boolean isPortableType(final Class<?> cls) {
    final MetaClass mc = MetaClassFactory.get(cls);

    final Set<MetaClass> exposedPortableTypes = new ErraiAppPropertiesConfiguration().modules()
            .getExposedPortableTypes();

    final Set<MetaClass> nonExposedPortableTypes = new ErraiAppPropertiesConfiguration().modules()
            .getNonExposedPortableTypes();

    return mc.isAnnotationPresent(Portable.class)
            || exposedPortableTypes.contains(mc)
            || nonExposedPortableTypes.contains(mc)
            || String.class.getName().equals(mc.getFullyQualifiedName())
            || TypeHandlerFactory.getHandler(mc.unsafeAsClass()) != null;
  }

  public static Set<MetaClass> allPortableConcreteSubtypes(final ErraiConfiguration erraiConfiguration,
          final MetaClassFinder metaClassFinder,
          final MetaClass metaClass) {

    final Set<MetaClass> allPortableConcreteSubtypes = allPortableTypes(metaClassFinder, erraiConfiguration).stream()
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

    return metaClass.instanceOf(String.class) || isBuiltinPortable(metaClass) || allPortableTypes(metaClassFinder,
            erraiConfiguration).contains(metaClass);
  }

  private static Set<MetaClass> allPortableTypes(final MetaClassFinder metaClassFinder,
          final ErraiConfiguration erraiConfiguration) {

    return metaClassFinder.extend(Portable.class, () -> allRemoteTypesReturnTypesAndParametersTypes(metaClassFinder))
            .extend(Portable.class, erraiConfiguration.modules()::getExposedPortableTypes)
            .extend(Portable.class, erraiConfiguration.modules()::getNonExposedPortableTypes)
            .findAnnotatedWith(Portable.class);
  }

  private static Collection<MetaClass> allRemoteTypesReturnTypesAndParametersTypes(final MetaClassFinder metaClassFinder) {
    return metaClassFinder.findAnnotatedWith(EnvironmentConfigExtension.class).stream().map(s -> {
      try {
        return Class.forName(s.getFullyQualifiedName()).asSubclass(ExposedTypesProvider.class).newInstance();
      } catch (final Exception e) {
        throw new RuntimeException(e); //FIXME: tiago:
      }
    }).flatMap(p -> p.provideTypesToExpose().stream()).collect(toSet());
  }

  //FIXME: Cache this map
  private static boolean isBuiltinPortable(final MetaClass metaClass) {
    if (!TYPE_HANDLERS.containsKey(metaClass) && TYPE_HANDLERS_INHERITANCE_MAP.containsKey(metaClass)) {
      return isBuiltinPortable(TYPE_HANDLERS_INHERITANCE_MAP.get(metaClass));
    } else {
      return TYPE_HANDLERS.get(metaClass) != null;
    }
  }
}
