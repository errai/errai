package org.jboss.errai.security.server.properties;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Properties;

import javax.inject.Qualifier;

/**
 * Used to get a {@link Properties} instance for the ErraiApp.properties resource.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Documented
@Qualifier
@Retention(RUNTIME)
@Target({ FIELD, TYPE, PARAMETER, METHOD })
public @interface ErraiAppProperties {
}
