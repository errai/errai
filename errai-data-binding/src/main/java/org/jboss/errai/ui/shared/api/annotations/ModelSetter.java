package org.jboss.errai.ui.shared.api.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

import org.jboss.errai.databinding.client.api.DataBinder;

/**
 * Indicates that the annotated method is used to replace a model instance managed by a
 * {@link DataBinder}. The method is required to have a single parameter. The parameter type needs
 * to correspond to the type of the managed model (see {@link Model} and {@link AutoBound}).
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Mike Brock
 */
@Documented
@Qualifier
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ModelSetter {
}
