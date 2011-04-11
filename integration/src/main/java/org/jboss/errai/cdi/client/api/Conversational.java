package org.jboss.errai.cdi.client.api;

import javax.interceptor.InterceptorBinding;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Mike Brock .
 */
@InterceptorBinding
@Target({ METHOD, ElementType.TYPE})
@Retention(RUNTIME)
@Documented
public @interface Conversational {
}
