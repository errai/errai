package org.jboss.errai.validation.client;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Johannes Barop <jb@barop.de>
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = TestConstraintValidator.class)
public @interface TestConstraint {

  String message() default "not lower case";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

}
