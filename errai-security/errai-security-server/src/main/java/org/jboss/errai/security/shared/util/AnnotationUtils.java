/**
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.security.shared.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;
import org.jboss.errai.security.shared.spi.RequiredRolesExtractor;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class AnnotationUtils {

  /**
   * Merge roles from multiple {@link RestrictedAccess}.
   *
   * @param accessRestrictions {@link RestrictedAccess} annotations with roles to be merged.
   *
   * @return All of the unique roles from the given annotations (including from providers).
   */
  public static Set<Role> mergeRoles(final RequiredRolesExtractor roleExtractor,
          final RestrictedAccess... accessRestrictions) {
    return mergeRoles(roleExtractor, Arrays.asList(accessRestrictions));
  }

  /**
   * Merge roles from multiple {@link RestrictedAccess}.
   *
   * @param accessRestrictions {@link RestrictedAccess} annotations with roles to be merged.
   *
   * @return All of the unique roles from the given annotations (including from providers).
   */
  public static Set<Role> mergeRoles(final RequiredRolesExtractor roleExtractor, final Iterable<RestrictedAccess> accessRestrictions) {
    final Set<Role> roles = new HashSet<Role>();

    for (final RestrictedAccess annotation : accessRestrictions) {
      if (annotation != null) {
        roles.addAll(roleExtractor.extractAllRoles(annotation));
      }
    }

    return roles;
  }
}
