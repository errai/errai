package org.jboss.errai.security.demo.client.local;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.jboss.errai.ui.shared.api.annotations.style.StyleBinding;

/**
 * Used to hide elements only appearing if Keycloak is setup.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Documented
@StyleBinding
@Target({ FIELD, METHOD })
@Retention(RUNTIME)
public @interface Keycloak {

}
