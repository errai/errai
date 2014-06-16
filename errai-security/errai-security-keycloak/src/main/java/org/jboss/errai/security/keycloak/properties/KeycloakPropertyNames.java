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

  public static final String ADDRESS = "address";

}
