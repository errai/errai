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

package org.jboss.errai.validation.client.api;

import java.util.Set;

import javax.inject.Singleton;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.metadata.BeanDescriptor;

import org.jboss.errai.databinding.client.BindableProxy;
import org.jboss.errai.databinding.client.api.Bindable;

/**
 * Injectable wrapper for the generated {@link Validator} to support validation of {@link Bindable} types.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Singleton
public class BeanValidator implements Validator {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

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
  public BeanDescriptor getConstraintsForClass(Class<?> clazz) {
    return validator.getConstraintsForClass(clazz);
  }

  @Override
  public <T> T unwrap(Class<T> type) {
    return validator.unwrap(type);
  }

  @SuppressWarnings("unchecked")
  private <T> T maybeUnwrapBindable(T object) {
    if (object instanceof BindableProxy) {
      object = (T) ((BindableProxy<T>) object).unwrap();
    }
    return object;
  }

}
