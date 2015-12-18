package org.jboss.errai.ui.client.widget;

import javax.inject.Qualifier;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Qualifies a {@link ListWidget} so that an ordered list (<ol>) is used for rendering the item list.
 * 
 * @author edewit@redhat.com
 */
@Target({ METHOD, PARAMETER, FIELD })
@Retention(RUNTIME)
@Documented
@Qualifier
public @interface OrderedList {
}
