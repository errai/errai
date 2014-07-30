package org.jboss.errai.validation.client.test;

import javax.validation.Validation;
import javax.validation.Validator;

import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ioc.client.test.AbstractErraiIOCTest;
import org.jboss.errai.validation.client.BlacklistedWithConstraint;
import org.jboss.errai.validation.client.blacklisted.ClassWithConstraintInBlacklistedPackage;
import org.junit.Test;

public class ValidationBlacklistTest extends AbstractErraiIOCTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.validation.ValidationTestModule";
  }

  @Test
  public void testBlacklistedBeanNotFound() throws Exception {
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    BlacklistedWithConstraint bean = DataBinder.forModel(new BlacklistedWithConstraint()).getModel();
    try {
      validator.validate(bean);
    }
    catch (IllegalArgumentException e) {
      return;
    }
    fail("There should not be a validator for the blacklisted bean.");
  }

  @Test
  public void testBeanInBlacklistedPackageNotFound() throws Exception {
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    ClassWithConstraintInBlacklistedPackage bean = DataBinder.forModel(new ClassWithConstraintInBlacklistedPackage())
            .getModel();
    try {
      validator.validate(bean);
    }
    catch (IllegalArgumentException e) {
      return;
    }
    fail("There should not be a validator for beans in blacklisted packages.");
  }

}
