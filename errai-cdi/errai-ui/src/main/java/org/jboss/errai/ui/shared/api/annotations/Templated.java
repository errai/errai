package org.jboss.errai.ui.shared.api.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.gwt.user.client.ui.Composite;

/**
 * Declares a class extending from {@link Composite} as participating in the
 * Errai UI templating framework.
 * 
 * <p>
 * A corresponding ComponentName.html file must be placed in the same directory
 * on the class-path as the custom ComponentName type.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Templated {
}
