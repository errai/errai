package org.jboss.errai.databinding.client.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jboss.errai.databinding.rebind.BindableProxyGenerator;

/**
 * Indicates that method or field should be ignored by the {@link BindableProxyGenerator}
 * 
 * @author Sašo Petrovič <saso.petrovic@gmail.com>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface IgnoreBinding {

}
