package org.jboss.errai.databinding.client.api;

/**
 * Specifies the state from which a {@link DataBinder}'s properties should be initialized.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public enum InitialState {
  
  /**
   * Specifies that the bound value should be initialized to the pre-existing
   * value in the model.
   */
  FROM_MODEL {
    @Override
    public <T> T getInitialValue(T modelValue, T uiValue) {
      return modelValue;
    }
  },
  
  /**
   * Specifies that the bound value should be initialized to the pre-existing
   * value in the UI widget.
   */
  FROM_UI {
    @Override
    public <T> T getInitialValue(T modelValue, T uiValue) {
      return uiValue;
    }
  };
  
  /**
   * Returns the model value or the UI value, as appropriate.
   * 
   * @param modelValue
   *          The pre-existing model value
   * @param uiValue
   *          The pre-existing UI widget value.
   * @return Either modelValue or uiValue. Return value will be null if the
   *         corresponding parameter value is null.
   */
  public abstract <T> T getInitialValue(T modelValue, T uiValue);
}
