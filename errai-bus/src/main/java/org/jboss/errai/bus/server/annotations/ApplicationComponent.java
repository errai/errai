package org.jboss.errai.bus.server.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Similier to @ApplicationScoped in the CDI specification.  This annotation provides a way to construct regular
 * bean on the server.  If you're using a CDI-based environment, you should use @ApplicationScoped instead.
 *
 * User: christopherbrock
 * Date: 19-Jul-2010
 * Time: 4:43:09 PM
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ApplicationComponent {
}
