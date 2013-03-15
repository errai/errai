package org.jboss.errai.ui.shared.api.annotations;

import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Mike Brock
 */
@Documented
@Qualifier
@Target({ ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ModelSetter {
}
