package org.jboss.errai.security.client.local.context;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Used to denote a simpler implementation that is likely used as a component of
 * a more complex implementation.
 * 
 * For example, {@link SecurityContext} extends {@link ActiveUserCache}. But the
 * default implementation of {@code SecurityContext} uses a {@code @Basic}
 * {@code ActiveUserCache} to implement the lesser functionality.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
@Documented
@Qualifier
public @interface Simple {
}
