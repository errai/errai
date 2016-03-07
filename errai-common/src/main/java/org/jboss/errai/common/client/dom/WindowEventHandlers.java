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
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/WindowEventHandlers">Web API</a>
 */
@JsType(isNative = true)
public interface WindowEventHandlers {
  @JsProperty EventListener getOnafterprint();
  @JsProperty void setOnafterprint(EventListener onafterprint);

  @JsProperty EventListener getOnbeforeprint();
  @JsProperty void setOnbeforeprint(EventListener onbeforeprint);

  @JsProperty EventListener getOnbeforeunload();
  @JsProperty void setOnbeforeunload(EventListener onbeforeunload);

  @JsProperty EventListener getOnhashchange();
  @JsProperty void setOnhashchange(EventListener onhashchange);

  @JsProperty EventListener getOnlanguagechange();
  @JsProperty void setOnlanguagechange(EventListener onlanguagechange);

  @JsProperty EventListener getOnmessage();
  @JsProperty void setOnmessage(EventListener onmessage);

  @JsProperty EventListener getOnoffline();
  @JsProperty void setOnoffline(EventListener onoffline);

  @JsProperty EventListener getOnonline();
  @JsProperty void setOnonline(EventListener ononline);

  @JsProperty EventListener getOnpagehide();
  @JsProperty void setOnpagehide(EventListener onpagehide);

  @JsProperty EventListener getOnpageshow();
  @JsProperty void setOnpageshow(EventListener onpageshow);

  @JsProperty EventListener getOnpopstate();
  @JsProperty void setOnpopstate(EventListener onpopstate);

  @JsProperty EventListener getOnresize();
  @JsProperty void setOnresize(EventListener onresize);

  @JsProperty EventListener getOnstorage();
  @JsProperty void setOnstorage(EventListener onstorage);

  @JsProperty EventListener getOnunhandledrejection();
  @JsProperty void setOnunhandledrejection(EventListener onunhandledrejection);

  @JsProperty EventListener getOnunload();
  @JsProperty void setOnunload(EventListener onunload);
}
