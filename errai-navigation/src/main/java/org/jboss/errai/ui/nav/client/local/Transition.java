package org.jboss.errai.ui.nav.client.local;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Widget;

/**
 * This annotation only applies to fields of templated composite widgets that
 * are of type {@link Anchor}. You can annotate such fields in order to indicate
 * the target (href) of the Anchor. Errai Navigation will add the appropriate
 * boilerplate code to implement navigation to the Errai Navigation page
 * indicated in the annotation.
 */
@Inherited
@Documented
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Transition {

  /**
   * Specify the target {@link Widget} Class that is annotated with @Page as the
   * target of the navigation transition.
   */
  Class<? extends Widget> value();

}
