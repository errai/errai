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

package org.jboss.errai.codegen.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.jboss.errai.codegen.meta.MetaAnnotation;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.common.client.api.interceptor.FeatureInterceptor;
import org.jboss.errai.common.client.api.interceptor.InterceptedCall;
import org.jboss.errai.common.client.api.interceptor.InterceptsRemoteCall;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * A utility class that provides a list of interceptors for a given remote interface and method.
 */
public class InterceptorProvider {

  // Maps a feature interceptor annotation (i.e. RequiredRoles, RestrictAccess) to a list of
  // interceptors that should be triggered when this annotation is present.
  private final Multimap<MetaClass, MetaClass> featureInterceptors = ArrayListMultimap.create();

  // Maps a remote interface type to a list of interceptors that should be triggered for all
  // methods of this type.
  private final Multimap<MetaClass, MetaClass> standaloneInterceptors = ArrayListMultimap.create();

  @SuppressWarnings("unchecked")
  public InterceptorProvider(final Collection<? extends MetaClass> featureInterceptors,
          final Collection<? extends MetaClass> standaloneInterceptors) {

    for (final MetaClass interceptorClass : standaloneInterceptors) {
      Arrays.stream(interceptorClass.getAnnotation(InterceptsRemoteCall.class)
              .map(metaAnnotation -> metaAnnotation.valueAsArray(MetaClass[].class))
              .orElse(new MetaClass[] {})).forEach(e -> this.standaloneInterceptors.put(e, interceptorClass));
    }

    for (final MetaClass featureInterceptor : featureInterceptors) {
      Arrays.stream(featureInterceptor.getAnnotation(FeatureInterceptor.class)
              .map(metaAnnotation -> metaAnnotation.valueAsArray(MetaClass[].class))
              .orElse(new MetaClass[] {})).forEach(e -> this.featureInterceptors.put(e, featureInterceptor));
    }
  }

  /**
   * Returns the interceptors for the provided proxy type and method.
   *
   * @param type   the remote interface
   * @param method the remote method
   * @return the list of interceptors that should be triggered when invoking the provided proxy
   * method on the provided type, never null.
   */
  @SuppressWarnings("unchecked")
  public List<MetaClass> getInterceptors(final MetaClass type, final MetaMethod method) {
    final List<MetaClass> interceptors = new ArrayList<>();

    Optional<MetaAnnotation> interceptedCall = method.getAnnotation(InterceptedCall.class);
    if (!interceptedCall.isPresent()) {
      interceptedCall = type.getAnnotation(InterceptedCall.class);
    }

    if (!interceptedCall.isPresent()) {
      interceptors.addAll(standaloneInterceptors.get(type));
    } else {
      final MetaClass[] value = interceptedCall.get().valueAsArray(MetaClass[].class);
      interceptors.addAll(Arrays.asList(value));
    }

    for (final MetaClass annotation : featureInterceptors.keySet()) {
      if (type.isAnnotationPresent(annotation) || method.isAnnotationPresent(annotation)) {
        interceptors.addAll(featureInterceptors.get(annotation));
      }
    }

    return interceptors;
  }
}
