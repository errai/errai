/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.validation.client;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.metadata.BeanDescriptor;

import de.knightsoftnet.validators.client.impl.Validation;

import org.jboss.errai.databinding.client.BindableProxy;
import org.jboss.errai.databinding.client.api.Bindable;

import de.knightsoftnet.validators.client.impl.AbstractGwtValidator;
import de.knightsoftnet.validators.client.impl.AbstractGwtSpecificValidator;
import de.knightsoftnet.validators.client.impl.GwtValidationContext;

/**
 * Wrapper for the generated {@link Validator} to support validation of {@link Bindable} types.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class BeanValidator extends AbstractGwtValidator {

  private final AbstractGwtValidator validator;

  public BeanValidator(AbstractGwtValidator validator) {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

    if (validator != null) {
      this.validator = validator;
      this.validator.init(factory.getConstraintValidatorFactory(),
              factory.getMessageInterpolator(),
              factory.getTraversableResolver(),
              factory.getParameterNameProvider());
    }
    else {
      this.validator = new NoopValidator();
    }
  }

  @Override
  public <T> Set<ConstraintViolation<T>> validate(T object, Class<?>... groups) {
    return validator.validate(maybeUnwrapBindable(object), groups);
  }

  @Override
  public <T> Set<ConstraintViolation<T>> validateProperty(T object, String propertyName, Class<?>... groups) {
    return validator.validateProperty(maybeUnwrapBindable(object), propertyName, groups);
  }

  @Override
  public <T> Set<ConstraintViolation<T>> validateValue(Class<T> beanType, String propertyName, Object value,
      Class<?>... groups) {

    return validator.validateValue(beanType, propertyName, maybeUnwrapBindable(value), groups);
  }

  @Override
  public <T> Set<ConstraintViolation<T>> validate(GwtValidationContext<T> context, Object object, Class<?>... groups)
      throws ValidationException {

    return validator.validate(context, maybeUnwrapBindable(object), groups);
  }

  @Override
  public BeanDescriptor getConstraintsForClass(Class<?> clazz) {
    return validator.getConstraintsForClass(clazz);
  }

  @Override
  public <T> T unwrap(Class<T> type) {
    return validator.unwrap(type);
  }

  @Override
  protected <T> AbstractGwtSpecificValidator<T> getValidatorForInstanceClass(final Object object) {
    return null;
  }

  @SuppressWarnings("unchecked")
  private <T> T maybeUnwrapBindable(T object) {
    if (object instanceof BindableProxy) {
      object = (T) ((BindableProxy<T>) object).deepUnwrap();
    }
    return object;
  }
}
