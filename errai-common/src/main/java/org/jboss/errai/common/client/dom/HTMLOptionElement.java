/**
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.common.client.dom;

import org.jboss.errai.common.client.api.annotations.Element;

import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@JsType(isNative = true)
@Element("option")
public interface HTMLOptionElement extends HTMLElement {
  @JsProperty HTMLFormElement getForm();

  @JsProperty boolean getDefaultSelected();
  @JsProperty void setDefaultSelected(boolean defaultSelected);

  @JsProperty String getText();
  @JsProperty void setText(String text);

  @JsProperty int getIndex();

  @Override
  @JsProperty boolean getDisabled();
  @JsProperty void setDisabled(boolean disabled);

  @Override
  @JsProperty String getLabel();
  @JsProperty void setLabel(String label);

  @JsProperty boolean getSelected();
  @JsProperty void setSelected(boolean selected);

  @JsProperty String getValue();
  @JsProperty void setValue(String value);
}
