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

package org.jboss.errai.security.keycloak.properties;

import org.jboss.errai.security.shared.api.identity.User;
import org.keycloak.representations.AccessToken;

/**
 * Names used to store properties in {@link User#getProperties()} that have been extracted from the
 * Keycloak {@link AccessToken}.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class KeycloakPropertyNames {

  public static final String PHONENUMBER_VERIFIED = "phonenumber-verified";

  public static final String EMAIL_VERIFIED = "email-verified";

  public static final String ZONE_INFO = "zone-info";

  public static final String WEBSITE = "website";

  public static final String SUBJECT = "subject";

  public static final String STREET_ADDRESS = "street-address";

  public static final String REGION = "region";

  public static final String PROFILE = "profile";

  public static final String PREFERRED_USERNAME = "preferred-username";

  public static final String POSTAL_CODE = "postal-code";

  public static final String PICTURE = "picture";

  public static final String PHONENUMBER = "phonenumber";

  public static final String NICKNAME = "nickname";

  public static final String NAME = "name";

  public static final String MIDDLE_NAME = "middle-name";

  public static final String LOCALITY = "locality";

  public static final String LOCALE = "locale";

  public static final String GENDER = "gender";

  public static final String FORMATTED_ADDRESS = "formatted-address";

  public static final String COUNTRY = "country";

  public static final String BIRTHDATE = "birthdate";

  public static final String AUDIENCE = "audience";

}
