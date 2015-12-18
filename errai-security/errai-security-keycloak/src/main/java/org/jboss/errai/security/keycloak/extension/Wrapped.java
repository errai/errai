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

package org.jboss.errai.security.keycloak.extension;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

import org.jboss.errai.security.keycloak.KeycloakAuthenticationService;
import org.jboss.errai.security.shared.service.AuthenticationService;

/**
 * This qualifier is added to non-Keycloak {@link AuthenticationService} implementations by the
 * {@link AuthenticationServiceWrapperExtension} so that the {@link KeycloakAuthenticationService} can
 * be used to extend the behaviour of the existing implementation.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Documented
@Retention(RUNTIME)
@Target({ TYPE, FIELD, PARAMETER, METHOD })
@Qualifier
public @interface Wrapped {
}
