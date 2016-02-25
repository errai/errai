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
@Element("tr")
public interface TableRowElement extends HTMLElement {
  @JsProperty int getRowIndex();
  @JsProperty int getSectionRowIndex();
  @JsProperty HTMLCollection getCells();

  @JsProperty String getAlign();
  @JsProperty void setAlign(String align);

  @JsProperty String getBgColor();
  @JsProperty void setBgColor(String bgColor);

  @JsProperty String getCh();
  @JsProperty void setCh(String ch);

  @JsProperty String getChOff();
  @JsProperty void setChOff(String chOff);

  @JsProperty String getVAlign();
  @JsProperty void setVAlign(String vAlign);

  HTMLElement insertCell(int index);
  void deleteCell(int index);
}
