package org.jboss.errai.cdi.stereotypes.client;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Stereotype;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Stereotype
@Target({ TYPE, METHOD, FIELD })
@Retention(RUNTIME)
@ApplicationScoped
@Inherited
public @interface AnimalStereotype {

}