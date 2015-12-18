/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.ui.nav.client.local;

import java.lang.annotation.Annotation;

import javax.inject.Singleton;

import org.jboss.errai.ioc.client.api.ContextualTypeProvider;
import org.jboss.errai.ioc.client.api.IOCProvider;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
@IOCProvider @Singleton
public class TransitionToRoleProvider implements ContextualTypeProvider<TransitionToRole<?>> {

  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public TransitionToRole provide(final Class<?>[] typeargs, final Annotation[] qualifiers) {
    final Class<UniquePageRole> uniquePageRole = (Class<UniquePageRole>) typeargs[0];
    return new TransitionToRole<UniquePageRole>(uniquePageRole);
  }

}
