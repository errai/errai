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

package org.jboss.errai.enterprise.rebind;

import java.util.Collection;
import java.util.Optional;

import org.jboss.errai.codegen.meta.MetaAnnotation;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.enterprise.client.jaxrs.ClientExceptionMapper;
import org.jboss.errai.enterprise.shared.api.annotations.MapsFrom;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class Utils {
  private Utils() {}

  public static Multimap<MetaClass, MetaClass> getClientExceptionMappers(final Collection<? extends MetaClass> providers) {
    final Multimap<MetaClass, MetaClass> result = ArrayListMultimap.create();

    MetaClass genericExceptionMapperClass = null;
    for (final MetaClass metaClass : providers) {
      if (!metaClass.isAbstract() && metaClass.isAssignableTo(ClientExceptionMapper.class)) {
        final Optional<MetaAnnotation> mapsFrom = metaClass.getAnnotation(MapsFrom.class);
        if (!mapsFrom.isPresent()) {
          if (genericExceptionMapperClass == null) {
            // Found a generic client-side exception mapper (to be used for all REST interfaces)
            genericExceptionMapperClass = metaClass;
            result.put(genericExceptionMapperClass, null);
          }
          else {
            throw new RuntimeException("Found two generic client-side exception mappers: "
                    + genericExceptionMapperClass.getFullyQualifiedName() + " and " + metaClass + ". Make use of "
                    + MapsFrom.class.getName() + " to resolve this problem.");
          }
        }
        else {
          final MetaClass[] remotes = mapsFrom.get().valueAsArray(MetaClass[].class);
          if (remotes != null) {
            for (final MetaClass remote : remotes) {
              result.put(metaClass, remote);
            }
          }
        }
      }
    }

    return result;
  }

}
