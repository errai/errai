package org.jboss.errai.validation.client;

import javax.validation.Validator;

import org.junit.Ignore;

import com.google.gwt.core.client.GWT;
import com.google.gwt.validation.client.AbstractGwtValidatorFactory;
import com.google.gwt.validation.client.GwtValidation;
import com.google.gwt.validation.client.impl.AbstractGwtValidator;

/**
 * GWT validation requires the presence of this factory to define the classes it generates validators for. One future
 * goal of the errai-validation module is to make this optional.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Ignore
public final class TestModelValidatorFactory extends AbstractGwtValidatorFactory {

  @GwtValidation(value = TestModel.class)
  public interface GwtValidator extends Validator {}

  @Override
  public AbstractGwtValidator createValidator() {
    return GWT.create(GwtValidator.class);
  }
}