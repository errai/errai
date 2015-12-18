/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.forge.facet.ui.command.res;

import org.jboss.forge.addon.convert.Converter;
import org.jboss.forge.addon.facets.FacetNotFoundException;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.facets.HintsFacet;
import org.jboss.forge.addon.ui.input.UICompleter;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.ValueChangeListener;
import org.jboss.forge.addon.ui.validate.UIValidator;
import org.jboss.forge.furnace.spi.ListenerRegistration;

import javax.enterprise.inject.Alternative;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Callable;

@Alternative
public class UIInputMock<T> implements UIInput<T> {
  
  private String name = "", label = "", description = "", requiredMessage = "";
  private boolean enabled, required;
  
  private T value;
  private Converter<String, T> converter;
  private T defaultValue;
  private UICompleter<T> completer;

  @Override
  public String getLabel() {
    return label;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public Class<T> getValueType() {
    return (Class<T>) value.getClass();
  }

  @Override
  public boolean hasDefaultValue() {
    return defaultValue != null;
  }

  @Override
  public boolean hasValue() {
    return value != null;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public boolean isRequired() {
    return required;
  }

  @Override
  public String getRequiredMessage() {
    return requiredMessage;
  }

  @Override
  public char getShortName() {
    return 0;
  }

  @Override
  public Set<UIValidator> getValidators() {
    return Collections.emptySet();
  }

  @Override
  public UIInput<T> setEnabled(boolean b) {
    enabled = b;
    return this;
  }

  @Override
  public UIInput<T> setEnabled(Callable<Boolean> callable) {
    return this;
  }

  @Override
  public UIInput<T> setLabel(String label) {
    this.label = label;
    return this;
  }

  @Override
  public UIInput<T> setDescription(String description) {
    this.description = description;
    return this;
  }

  @Override
  public UIInput<T> setDescription(Callable<String> description){
    try {
      this.description = description.call();
    } catch (Exception e) {}
    return this;
  }

  @Override
  public UIInput<T> setRequired(boolean required) {
    this.required = required;
    return this;
  }

  @Override
  public UIInput<T> setRequired(Callable<Boolean> required) {
    return this;
  }

  @Override
  public UIInput<T> setRequiredMessage(String message) {
    this.requiredMessage = message;
    return this;
  }

  @Override
  public Converter<String, T> getValueConverter() {
    return converter;
  }

  @Override
  public UIInput<T> setValueConverter(Converter<String, T> converter) {
    this.converter = converter;
    return this;
  }

  @Override
  public UIInput<T> addValidator(UIValidator validator) {
    return this;
  }

  @Override
  public T getValue() {
    return (hasValue() ? value : defaultValue);
  }

  @Override
  public ListenerRegistration<ValueChangeListener> addValueChangeListener(ValueChangeListener listener) {
    return null;
  }

  @Override
  public void validate(UIValidationContext context) {
  }

  @Override
  public boolean install(HintsFacet facet) {
    return false;
  }

  @Override
  public boolean register(HintsFacet facet) {
    return false;
  }

  @Override
  public boolean unregister(HintsFacet facet) {
    return false;
  }

  @Override
  public boolean uninstall(HintsFacet facet) {
    return false;
  }

  @Override
  public boolean hasFacet(Class<? extends HintsFacet> type) {
    return false;
  }

  @Override
  public boolean hasAllFacets(Class<? extends HintsFacet>... facetDependencies) {
    return false;
  }

  @Override
  public boolean hasAllFacets(Iterable<Class<? extends HintsFacet>> facetDependencies) {
    return false;
  }

  @Override
  public <F extends HintsFacet> F getFacet(Class<F> type) throws FacetNotFoundException {
    return null;
  }

  @Override
  public Iterable<HintsFacet> getFacets() {
    return Collections.emptyList();
  }

  @Override
  public <F extends HintsFacet> Iterable<F> getFacets(Class<F> type) {
    return Collections.emptyList();
  }

  @Override
  public <F extends HintsFacet> boolean supports(F facet) {
    return false;
  }

  @Override
  public UIInput<T> setDefaultValue(T defaultValue) {
    this.defaultValue = defaultValue;
    return this;
  }

  @Override
  public UIInput<T> setDefaultValue(Callable<T> callback) {
    return this;
  }

  @Override
  public UIInput<T> setValue(T value) {
    this.value = value;
    return this;
  }

  @Override
  public UICompleter<T> getCompleter() {
    return null;
  }

  @Override
  public UIInput<T> setCompleter(UICompleter<T> completer) {
    this.completer = completer;
    return this;
  }

}
