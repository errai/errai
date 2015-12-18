package org.jboss.errai.ui.client.widget;

import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Qualifies a {@link ListWidget} so that an HTML table is used for rendering the item list.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Target( {METHOD, PARAMETER, FIELD })
@Retention(RUNTIME)
@Documented
@Qualifier
public @interface Table {
  
  /*
   * The HTML tag to use as root element when rendering the table, defaults to "table", but can be changed to i.e. tbody.
   */
  String root() default "table";
}
