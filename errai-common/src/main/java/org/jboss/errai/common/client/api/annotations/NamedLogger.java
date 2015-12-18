package org.jboss.errai.common.client.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.slf4j.Logger;

/**
 * When injecting a {@link Logger}, this annotation can be used to specify a
 * name. If this annotation is used but no value is given, the root logger will
 * be provided.
 * 
 * Example usage:
 * 
 * <pre>
 * // Gets root logger
 * {@literal @Inject @NamedLogger} Logger logger;
 * 
 * // Gets logger with name 'LoggerName'
 * {@literal @Inject @NamedLogger("LoggerName")} Logger logger;
 * </pre>
 * 
 * @author mbarkley <mbarkley@redhat.com>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface NamedLogger {
  String value() default org.slf4j.Logger.ROOT_LOGGER_NAME;
}
