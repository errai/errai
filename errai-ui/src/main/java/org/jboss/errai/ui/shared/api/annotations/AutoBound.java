package org.jboss.errai.ui.shared.api.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jboss.errai.databinding.client.api.DataBinder;

/**
 * Indicates that the annotated {@link DataBinder} is used to automatically bind all enclosing {@link DataField}s to
 * properties of the corresponding data model (the model instance associated with the data binder instance). The
 * enclosing data fields are all fields annotated with {@link Bound} and {@link DataField} of the class that defines the
 * {@link DataBinder} and all its super classes.
 * <p>
 * This annotation is only useful in combination with a {@link DataBinder} field or a {@link DataBinder} constructor
 * parameter.
 * <p>
 * There can only be one auto bound data binder per {@link Templated} class.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Inherited
@Documented
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoBound {

}
