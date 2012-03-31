package org.jboss.errai.ioc.client.api;

import javax.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Provides a hint to the container that the annotated element should be loaded asynchronously using GWT's
 * code splitting. This can be placed on any container-managed element (beans or producers)..
 * <p>
 * This annotation will only be treated as a hint. Annotating a bean or producer does not guarantee that the container
 * will employ code splitting.
 *
 * @author Mike Brock
 */
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
@java.lang.annotation.Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
public @interface LoadAsync {
}
