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
@Target({ TYPE, FIELD, PARAMETER })
@Qualifier
public @interface Wrapped {

}
