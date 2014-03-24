package org.jboss.errai.security.client.local.context;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Used to denote a more complex implementation usually implemented using an
 * {@link Simple} implementation.
 * 
 * For example, {@link SecurityContext} extends {@link ActiveUserCache}. The
 * default implementation of {@code SecurityContext} is {@code @Complex}, and
 * uses a {@code @Simple ActiveUserCache} to implement the lesser functionality.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
@Documented
@Qualifier
public @interface Complex {
}
