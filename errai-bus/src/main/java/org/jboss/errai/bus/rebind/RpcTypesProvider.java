/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.rebind;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.jboss.errai.bus.server.annotations.Remote;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.meta.MetaParameterizedType;
import org.jboss.errai.codegen.meta.MetaType;
import org.jboss.errai.config.rebind.EnvironmentConfigExtension;
import org.jboss.errai.config.rebind.ExposedTypesProvider;
import org.jboss.errai.config.util.ClassScanner;

/**
 * @author Mike Brock
 */
@EnvironmentConfigExtension
public class RpcTypesProvider implements ExposedTypesProvider {
  @Override
  public Collection<MetaClass> provideTypesToExpose() {
    final Set<MetaClass> types = new HashSet<MetaClass>();
    for (final MetaClass metaClass : ClassScanner.getTypesAnnotatedWith(Remote.class)) {
      for (final MetaMethod method : metaClass.getDeclaredMethods()) {
        if (!method.getReturnType().isVoid()) {
          types.add(method.getReturnType().getErased());
        }
        for (final MetaParameter parameter : method.getParameters()) {
          final MetaClass type = parameter.getType();

          types.add(type.getErased());

          final MetaParameterizedType parameterizedType = type.getParameterizedType();
          if (parameterizedType != null) {
            for (final MetaType tp : parameterizedType.getTypeParameters()) {
               if (tp instanceof MetaClass) {
                 types.add(((MetaClass) tp).getErased());
               }
            }
          }

        }
      }
    }
    return types;
  }
}
