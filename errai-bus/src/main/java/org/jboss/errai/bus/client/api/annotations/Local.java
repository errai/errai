package org.jboss.errai.bus.client.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Local indicates the scope of the service, endpoint or any other bus enabled service should be
 * limited in scope to the <em>local</em> bus. Meaning quite plainly, that the service should only
 * be visible locally. If @Local is used on the client, then the service is only visible to other
 * client components. If @Local is used on the server, then the service is only visible to other
 * server components.</p>
 * <p/>
 * Note: Errai extensions that build on top of the bus and extend it's functionality should make
 * their best efforts to integrate this annotation and it's intended behaviour.</p>
 *
 * @author Mike Brock .
 */
@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({ElementType.FIELD, ElementType.TYPE, ElementType.METHOD})
public @interface Local {
}
