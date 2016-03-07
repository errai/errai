/**
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except compliance with the License.
 * You may obtaa copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.common.client.dom;

import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement">Web API</a>
 */
@JsType(isNative = true)
public interface HTMLElement extends Element, GlobalEventHandlers {
  NodeList getElementsByClassName(String classNames);

  @JsProperty String getInnerHTML();
  @JsProperty void setInnerHTML(String innerHtml);

  @JsProperty String getOuterHTML();
  @JsProperty void setOuterHTML(String outerHtml);

  void insertAdjacentHTML(String position, String text);

  @JsProperty String getId();
  @JsProperty void setId(String id);

  @JsProperty String getTitle();
  @JsProperty void setTitle(String title);

  @JsProperty String getLang();
  @JsProperty void setLang(String lang);

  @JsProperty String getDir();
  @JsProperty void setDir(String dir);

  @JsProperty String getClassName();
  @JsProperty void setClassName(String property);

  @JsProperty DOMTokenList getClassList();

  @JsProperty DOMStringMap getDataset();

  @JsProperty boolean getHidden();
  @JsProperty void setHidden(boolean hidden);

  void click();

  @JsProperty int getTabIndex();
  @JsProperty void setTabIndex(int tabIndex);

  void focus();
  void blur();

  @JsProperty String getAccessKey();
  @JsProperty void setAccessKey(String accessKey);

  @JsProperty String getAccessKeyLabel();

  @JsProperty String getBaseURI();

  @JsProperty String getLocalName();

  @JsProperty String getNamespaceURI();

  @JsProperty boolean getDraggable();
  @JsProperty void setDraggable(boolean draggable);

  @JsProperty DOMSettableTokenList getDropzone();
  @JsProperty void setDropzone(DOMSettableTokenList tokenList);

  @JsProperty String getContentEditable();
  @JsProperty void setContentEditable(String contentEditable);

  @JsProperty(name="isContentEditable") boolean isContentEditable();

  @JsProperty Menu getContextMenu();
  @JsProperty void setContextMenu(Menu menuElement);

  @JsProperty boolean getSpellcheck();
  @JsProperty void setSpellcheck(boolean spellcheck);

  @JsProperty String getCommandType();

  @JsProperty String getLabel();

  @JsProperty String getIcon();

  @JsProperty boolean getDisabled();

  @JsProperty boolean getChecked();

  @JsProperty CSSStyleDeclaration getStyle();

  @JsProperty EventListener getOncopy();
  @JsProperty void setOncopy(EventListener oncopy);

  @JsProperty EventListener getOncut();
  @JsProperty void setOncut(EventListener oncut);

  @JsProperty EventListener getOnpaste();
  @JsProperty void setOnpaste(EventListener onpaste);
}
