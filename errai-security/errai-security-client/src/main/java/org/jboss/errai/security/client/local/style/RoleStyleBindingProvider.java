/*
 * Copyright (C) 2014 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.security.client.local.style;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.security.client.local.spi.ActiveUserCache;
import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.spi.RequiredRolesExtractor;
import org.jboss.errai.ui.shared.api.style.AnnotationStyleBindingExecutor;
import org.jboss.errai.ui.shared.api.style.StyleBindingsRegistry;

import com.google.gwt.user.client.Element;

/**
 * RoleStyleBindingProvider makes sure that client elements annotated by {@link RestrictedAccess} are made invisible for
 * users that do not have the role or roles specified.
 *
 * @see RestrictedAccess
 * @author edewit@redhat.com
 * @author Max Barkley <mbarkley@redhat.com>
 */
@EntryPoint
@SuppressWarnings("deprecation")
public class RoleStyleBindingProvider {

  private final ActiveUserCache userCache;
  private final RequiredRolesExtractor roleExtractor;

  @Inject
  public RoleStyleBindingProvider(final ActiveUserCache userProvider, final RequiredRolesExtractor roleExtractor) {
    this.userCache = userProvider;
    this.roleExtractor = roleExtractor;
  }

  @PostConstruct
  public void init() {
    StyleBindingsRegistry.get().addStyleBinding(RestrictedAccess.class, new AnnotationStyleBindingExecutor() {
      @Override
      public void invokeBinding(final Element element, final Annotation annotation) {
        final User user = userCache.getUser();
        final Set<Role> extractedRoles = roleExtractor.extractAllRoles((RestrictedAccess) annotation);

        if (User.ANONYMOUS.equals(user) || !user.getRoles().containsAll(extractedRoles)) {
          element.addClassName(RestrictedAccess.CSS_CLASS_NAME);
        }
        else {
          element.removeClassName(RestrictedAccess.CSS_CLASS_NAME);
        }
      }
    });
  }
}
