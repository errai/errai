package org.jboss.errai.ui.shared.api.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jboss.errai.databinding.client.api.Converter;
import org.jboss.errai.databinding.client.api.DataBinder;

/**
 * Indicates that a {@link DataField} is automatically bound to a property of a data model associated with a
 * {@link DataBinder} (see {@link AutoBound}).
 * <p>
 * If no property is specified, the {@link DataField} is bound to the data model property with matching name.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Inherited
@Documented
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@SuppressWarnings("rawtypes")
public @interface Bound {

  /**
   * The name of the data model property to bind the {@link DataField} to, following Java bean conventions. If omitted,
   * the data field will be bound to the data model property with matching name.
   */
  String property() default "";

  /**
   * The {@link Converter} to use when setting values on the model or widget.
   */
  Class<? extends Converter> converter() default NO_CONVERTER.class;

  static abstract class NO_CONVERTER implements Converter {}
}
