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
import javax.validation.Validator;

import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.test.AbstractErraiIOCTest;
import org.jboss.errai.validation.client.ModuleWithInjectedValidator;
import org.jboss.errai.validation.client.TestModel;
import org.jboss.errai.validation.client.api.BeanValidator;

/**
 * Integration test for injected {@link BeanValidator}s.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
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
    Validator validator = new BeanValidator();
    
    TestModel model = DataBinder.forModel(new TestModel()).getModel();
    Set<ConstraintViolation<TestModel>> violations = validator.validate(model);
    assertEquals("Expected two constraint violations", 2, violations.size());
  }
}