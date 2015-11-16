package org.jboss.errai.cdi.stereotypes.client;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import javax.enterprise.inject.Stereotype;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Stereotype
@Target({ TYPE })
@Retention(RUNTIME)
public @interface HornedMammalStereotype {

}