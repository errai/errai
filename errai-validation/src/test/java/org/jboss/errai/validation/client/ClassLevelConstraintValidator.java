package org.jboss.errai.validation.client;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author Divya Dadlani <ddadlani@redhat.com>
 */
public class ClassLevelConstraintValidator implements ConstraintValidator<TestConstraint, ClassLevelConstraintBean> {
  @Override
  public void initialize(TestConstraint constraintAnnotation) {

  }

  @Override
  public boolean isValid(ClassLevelConstraintBean value, ConstraintValidatorContext context) {
    return value.getId() > 0;
  }
}
