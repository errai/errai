package org.jboss.errai.common.client.api.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Mark annotation as an alias. This will recursively add the annotations that are on the annotation for example:
 * <pre>
 * {@code @Alias}
 * {@code @Inject}
 * {@code @Bound}
 * {@code @DataField}
 *  public {@code @interface} UiProperty {
 *  }
 * </pre>
 *
 * The Alias in the example above will make sure that UiProperty is the same as specifying Inject, Bound and
 * DataField on a field.
 *
 * @author edewit@redhat.com
 */
@Deprecated
@Target(TYPE)
@Retention(RUNTIME)
public @interface Alias {
}
