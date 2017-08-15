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
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLTableCellElement">Web API</a>
 */
@JsType(isNative = true)
@Element({"th", "td"})
public interface TableCell extends HTMLElement {
  @JsProperty int getCellIndex();

  @JsProperty String getAbbr();
  @JsProperty void setAbbr(String abbr);

  @JsProperty String getAlign();
  @JsProperty void setAlign(String align);

  @JsProperty String getAxis();
  @JsProperty void setAxis(String axis);

  @JsProperty String getBgColor();
  @JsProperty void setBgColor(String bgColor);

  @JsProperty String getCh();
  @JsProperty void setCh(String ch);

  @JsProperty String getChOff();
  @JsProperty void setChOff(String chOff);

  @JsProperty int getColSpan();
  @JsProperty void setColSpan(int colSpan);

  @JsProperty String getHeaders();
  @JsProperty void setHeaders(String headers);

  @JsProperty String getHeight();
  @JsProperty void setHeight(String height);

  @JsProperty boolean getNoWrap();
  @JsProperty void setNoWrap(boolean noWrap);

  @JsProperty int getRowSpan();
  @JsProperty void setRowSpan(int rowSpan);

  @JsProperty String getScope();
  @JsProperty void setScope(String scope);

  @JsProperty String getVAlign();
  @JsProperty void setVAlign(String vAlign);

  @JsProperty String getWidth();
  @JsProperty void setWidth(String width);
}
