package org.jboss.errai.cdi.client.api;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.interceptor.InterceptorBinding;

/**
 * @author Mike Brock .
 */
@InterceptorBinding @Target({METHOD,ElementType.TYPE}) @Retention(RUNTIME) @Documented public @interface Conversational {}
