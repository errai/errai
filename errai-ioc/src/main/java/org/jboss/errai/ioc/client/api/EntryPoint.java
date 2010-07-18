package org.jboss.errai.ioc.client.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * User: christopherbrock
 * Date: 18-Jul-2010
 * Time: 10:38:58 AM
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EntryPoint {
}
