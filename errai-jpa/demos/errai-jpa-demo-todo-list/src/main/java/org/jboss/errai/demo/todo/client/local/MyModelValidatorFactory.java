package org.jboss.errai.demo.todo.client.local;

import javax.validation.Validator;

import org.jboss.errai.demo.todo.shared.TodoItem;
import org.jboss.errai.demo.todo.shared.User;

import com.google.gwt.core.client.GWT;
import com.google.gwt.validation.client.AbstractGwtValidatorFactory;
import com.google.gwt.validation.client.GwtValidation;
import com.google.gwt.validation.client.impl.AbstractGwtValidator;

public final class MyModelValidatorFactory extends AbstractGwtValidatorFactory {

  @GwtValidation({TodoItem.class, User.class})
  public interface GwtValidator extends Validator {}

  @Override
  public AbstractGwtValidator createValidator() {
    return GWT.create(GwtValidator.class);
  }
}