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

import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@JsType(isNative = true)
public interface Document extends Node, GlobalEventHandlers {
  @JsProperty DocumentType getDoctype();
  @JsProperty DOMImplementation getImplementation();
  @JsProperty Element getDocumentElement();
  @JsProperty HTMLBodyElement getBody();

  HTMLElement createElement(String tagName);
  DocumentFragment createDocumentFragment();
  Text createTextNode(String data);
  Comment createComment(String data);
  CDATASection createCDATASection(String data);
  ProcessingInstruction createProcessingInstruction(String target, String data);
  Attr createAttribute(String name);
  EntityReference createEntityReference(String name);
  NodeList getElementsByTagName(String tagname);
}
