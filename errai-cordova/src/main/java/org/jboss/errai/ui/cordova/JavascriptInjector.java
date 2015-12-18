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

package org.jboss.errai.ui.cordova;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.HeadElement;
import com.google.gwt.dom.client.ScriptElement;

/**
 * @author edewit@redhat.com
 */
public class JavascriptInjector {
  private static HeadElement head;

  /**
   * Injects the JavaScript code into a
   * {@code <script type="text/javascript">...</script>} element in the
   * document header.
   *
   * @param javascript
   *            the JavaScript code
   */
  public static void inject(String javascript) {
    HeadElement head = getHead();
    ScriptElement element = createScriptElement();
    element.setText(javascript);
    head.appendChild(element);
  }

  private static HeadElement getHead() {
    if (head == null) {
      Element element = Document.get().getElementsByTagName("head").getItem(0);
      assert element != null : "HTML Head element required";
      JavascriptInjector.head = HeadElement.as(element);
    }
    return JavascriptInjector.head;
  }

  private static ScriptElement createScriptElement() {
    ScriptElement script = Document.get().createScriptElement();
    script.setAttribute("type", "text/javascript");
    script.setAttribute("charset", "UTF-8");
    return script;
  }
}
