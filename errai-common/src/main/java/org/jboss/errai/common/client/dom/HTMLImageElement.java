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
@Element("img")
public interface HTMLImageElement extends HTMLElement {
  @JsProperty String getName();
  @JsProperty void setName(String name);

  @JsProperty String getAlign();
  @JsProperty void setAlign(String align);

  @JsProperty String getAlt();
  @JsProperty void setAlt(String alt);

  @JsProperty String getBorder();
  @JsProperty void setBorder(String border);

  @JsProperty int getHeight();
  @JsProperty void setHeight(int height);

  @JsProperty int getHspace();
  @JsProperty void setHspace(int hspace);

  @JsProperty(name = "isMap") boolean isMap();
  @JsProperty(name = "isMap") void setMap(boolean map);

  @JsProperty String getLongDesc();
  @JsProperty void setLongDesc(String longDesc);

  @JsProperty String getSrc();
  @JsProperty void setSrc(String src);

  @JsProperty String getUseMap();
  @JsProperty void setUseMap(String useMap);

  @JsProperty int getVspace();
  @JsProperty void setVspace(int vspace);

  @JsProperty int getWidth();
  @JsProperty void setWidth(int width);
}
