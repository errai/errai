package org.jboss.errai.security.keycloak.extension;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

import org.jboss.errai.security.shared.service.AuthenticationService;

/**
 * This qualifier is used so that services wishing to access the {@link Wrapped}
 * {@link AuthenticationService} need not worry if one was provided. Instead, if no {@link Wrapped}
 * service exists, the implementation with this qualifier will be a dummy service. There should
 * always be exactly one {@link AuthenticationService} with this qualifier.
 *
 * @see WrappedServiceProducer
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Documented
@Retention(RUNTIME)
@Target({ TYPE, FIELD, PARAMETER, METHOD })
@Qualifier
public @interface Filtered {
}
