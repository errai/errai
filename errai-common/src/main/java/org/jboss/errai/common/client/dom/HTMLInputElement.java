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
@Element("input")
public interface HTMLInputElement extends HTMLElement {
  @JsProperty String getDefaultValue();
  @JsProperty void setDefaultValue(String defaultValue);

  @JsProperty boolean getDefaultChecked();
  @JsProperty void setDefaultChecked(boolean defaultChecked);

  @JsProperty HTMLFormElement getForm();

  @JsProperty String getAccept();
  @JsProperty void setAccept(String accept);

  @Override
  @JsProperty String getAccessKey();
  @Override
  @JsProperty void setAccessKey(String accessKey);

  @JsProperty String getAlign();
  @JsProperty void setAlign(String align);

  @JsProperty String getAlt();
  @JsProperty void setAlt(String alt);

  @Override
  @JsProperty boolean getChecked();
  @JsProperty void setChecked(boolean checked);

  @Override
  @JsProperty boolean getDisabled();
  @JsProperty void setDisabled(boolean disabled);

  @JsProperty int getMaxLength();
  @JsProperty void setMaxLength(int maxLength);

  @JsProperty String getName();
  @JsProperty void setName(String name);

  @JsProperty boolean getReadOnly();
  @JsProperty void setReadOnly(boolean readOnly);

  @JsProperty int getSize();
  @JsProperty void setSize(int size);

  @JsProperty String getSrc();
  @JsProperty void setSrc(String src);

  @Override
  @JsProperty int getTabIndex();
  @Override
  @JsProperty void setTabIndex(int tabIndex);

  @JsProperty String getType();
  @JsProperty void setType(String type);

  @JsProperty String getUseMap();
  @JsProperty void setUseMap(String useMap);

  @JsProperty String getValue();
  @JsProperty void setValue(String value);

  @Override
  void blur();
  @Override
  void focus();
  void select();
  @Override
  void click();
}
