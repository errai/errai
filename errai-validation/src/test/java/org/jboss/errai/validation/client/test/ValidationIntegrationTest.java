/*
 * Copyright 2011 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.validation.client.test;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.groups.Default;

import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.test.AbstractErraiIOCTest;
import org.jboss.errai.validation.client.ModuleWithInjectedValidator;
import org.jboss.errai.validation.client.TestGroup;
import org.jboss.errai.validation.client.TestModel;
import org.jboss.errai.validation.client.api.BeanValidator;

import com.google.gwt.user.client.ui.TextBox;

/**
 * Integration test for injected {@link BeanValidator}s.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Johannes Barop <jb@barop.de>
 */
public class ValidationIntegrationTest extends AbstractErraiIOCTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.validation.ValidationTestModule";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
  }

  public void testValidatorInjection() {
    ModuleWithInjectedValidator module =
      IOC.getBeanManager().lookupBean(ModuleWithInjectedValidator.class).getInstance();
    Validator validator = module.getValidator();
    assertNotNull("Validator was not injected", validator);
    
    Set<ConstraintViolation<TestModel>> violations = validator.validate(new TestModel());
    assertEquals("Expected two constraint violations", 2, violations.size());
  }
  
  public void testValidationOfBindableType() {
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    
    TestModel model = DataBinder.forModel(new TestModel()).getModel();
    Set<ConstraintViolation<TestModel>> violations = validator.validate(model);
    assertEquals("Expected two constraint violations", 2, violations.size());
    
    model.setStringVal("valid");
    violations = validator.validate(model);
    assertEquals("Expected one constraint violations", 1, violations.size());
    
    model.setNumVal(101);
    violations = validator.validate(model);
    assertEquals("Expected no constraint violations", 0, violations.size());
  }
  
  public void testValidationOfNestedBindableType() {
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    
    TestModel model = DataBinder.forModel(new TestModel()).bind(new TextBox(), "child.stringVal").getModel();
    Set<ConstraintViolation<TestModel>> violations = validator.validate(model);
    // model and child model should get validated (see @Valid on child field which caused validation when not null)
    assertEquals("Expected four constraint violations", 4, violations.size());
    
    model.setNumVal(101);
    violations = validator.validate(model);
    assertEquals("Expected three constraint violations", 3, violations.size());
    
    model.getChild().setStringVal("valid");
    violations = validator.validate(model);
    assertEquals("Expected two constraint violations", 2, violations.size());
  }

  public void testValidationOfBindableTypeWithInjectedValidator() {
    ModuleWithInjectedValidator module =
      IOC.getBeanManager().lookupBean(ModuleWithInjectedValidator.class).getInstance();
    Validator validator = module.getValidator();
    
    TestModel model = DataBinder.forModel(new TestModel()).getModel();
    Set<ConstraintViolation<TestModel>> violations = validator.validate(model);
    assertEquals("Expected two constraint violations", 2, violations.size());
    
    model.setStringVal("valid");
    violations = validator.validate(model);
    assertEquals("Expected one constraint violations", 1, violations.size());
    
    model.setNumVal(101);
    violations = validator.validate(model);
    assertEquals("Expected no constraint violations", 0, violations.size());
  }
  
  public void testValidationWithGroup() {
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    TestModel model = DataBinder.forModel(new TestModel()).getModel();
    Set<ConstraintViolation<TestModel>> violations = validator.validate(model, TestGroup.class);
    assertEquals("Expected one constraint violations", 1, violations.size());

    violations = validator.validate(model, Default.class, TestGroup.class);
    assertEquals("Expected three constraint violations", 3, violations.size());
  }

}