package org.jboss.errai.validation.client;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author Johannes Barop <jb@barop.de>
 */
public class TestConstraintValidator implements ConstraintValidator<TestConstraint, String> {

  @Override
  public void initialize(TestConstraint constraintAnnotation) {
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return value != null;
  }

}
