/*
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ui.rebind.ioc.element;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import elemental2.dom.Element;
import elemental2.dom.HTMLAnchorElement;
import elemental2.dom.HTMLAppletElement;
import elemental2.dom.HTMLAreaElement;
import elemental2.dom.HTMLAudioElement;
import elemental2.dom.HTMLBRElement;
import elemental2.dom.HTMLBaseElement;
import elemental2.dom.HTMLBaseFontElement;
import elemental2.dom.HTMLBodyElement;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLCanvasElement;
import elemental2.dom.HTMLDListElement;
import elemental2.dom.HTMLDataListElement;
import elemental2.dom.HTMLDetailsElement;
import elemental2.dom.HTMLDialogElement;
import elemental2.dom.HTMLDirectoryElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLEmbedElement;
import elemental2.dom.HTMLFieldSetElement;
import elemental2.dom.HTMLFontElement;
import elemental2.dom.HTMLFormElement;
import elemental2.dom.HTMLFrameElement;
import elemental2.dom.HTMLFrameSetElement;
import elemental2.dom.HTMLHRElement;
import elemental2.dom.HTMLHeadElement;
import elemental2.dom.HTMLHeadingElement;
import elemental2.dom.HTMLHtmlElement;
import elemental2.dom.HTMLIFrameElement;
import elemental2.dom.HTMLImageElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.HTMLIsIndexElement;
import elemental2.dom.HTMLLIElement;
import elemental2.dom.HTMLLabelElement;
import elemental2.dom.HTMLLegendElement;
import elemental2.dom.HTMLLinkElement;
import elemental2.dom.HTMLMapElement;
import elemental2.dom.HTMLMenuElement;
import elemental2.dom.HTMLMetaElement;
import elemental2.dom.HTMLMeterElement;
import elemental2.dom.HTMLModElement;
import elemental2.dom.HTMLOListElement;
import elemental2.dom.HTMLObjectElement;
import elemental2.dom.HTMLOptGroupElement;
import elemental2.dom.HTMLOptionElement;
import elemental2.dom.HTMLOutputElement;
import elemental2.dom.HTMLParagraphElement;
import elemental2.dom.HTMLParamElement;
import elemental2.dom.HTMLPreElement;
import elemental2.dom.HTMLProgressElement;
import elemental2.dom.HTMLQuoteElement;
import elemental2.dom.HTMLScriptElement;
import elemental2.dom.HTMLSelectElement;
import elemental2.dom.HTMLSourceElement;
import elemental2.dom.HTMLStyleElement;
import elemental2.dom.HTMLTableCaptionElement;
import elemental2.dom.HTMLTableCellElement;
import elemental2.dom.HTMLTableColElement;
import elemental2.dom.HTMLTableElement;
import elemental2.dom.HTMLTableRowElement;
import elemental2.dom.HTMLTableSectionElement;
import elemental2.dom.HTMLTemplateElement;
import elemental2.dom.HTMLTextAreaElement;
import elemental2.dom.HTMLTitleElement;
import elemental2.dom.HTMLTrackElement;
import elemental2.dom.HTMLUListElement;
import elemental2.dom.HTMLVideoElement;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
class Elemental2TagMapping {

  private static final Multimap<MetaClass, String> TAG_NAMES_BY_DOM_INTERFACE;

  static Collection<String> getTags(final MetaClass elemental2ElementClass) {

    if (elemental2ElementClass == null || metaClass(Element.class).equals(elemental2ElementClass)) {
      return Collections.emptyList();
    }

    final Collection<String> tags = TAG_NAMES_BY_DOM_INTERFACE.get(elemental2ElementClass);

    if (tags.isEmpty()) {
      return getTags(elemental2ElementClass.getSuperClass());
    }

    return tags;
  }

  static {
    TAG_NAMES_BY_DOM_INTERFACE = ImmutableMultimap.<MetaClass, String>builder()

            .put(metaClass(HTMLAnchorElement.class), "a")
            .put(metaClass(HTMLAppletElement.class), "applet")
            .put(metaClass(HTMLAreaElement.class), "area")
            .put(metaClass(HTMLAudioElement.class), "audio")
            .put(metaClass(HTMLBaseElement.class), "base")
            .put(metaClass(HTMLBaseFontElement.class), "basefont")
            .put(metaClass(HTMLBodyElement.class), "body")
            .put(metaClass(HTMLBRElement.class), "br")
            .put(metaClass(HTMLButtonElement.class), "button")
            .put(metaClass(HTMLCanvasElement.class), "canvas")
            .put(metaClass(HTMLDataListElement.class), "datalist")
            .put(metaClass(HTMLDetailsElement.class), "details")
            .put(metaClass(HTMLDialogElement.class), "dialog")
            .put(metaClass(HTMLDirectoryElement.class), "dir")
            .put(metaClass(HTMLDivElement.class), "div")
            .put(metaClass(HTMLDListElement.class), "dl")
            .put(metaClass(HTMLEmbedElement.class), "embed")
            .put(metaClass(HTMLFieldSetElement.class), "fieldset")
            .put(metaClass(HTMLFontElement.class), "font")
            .put(metaClass(HTMLFormElement.class), "form")
            .put(metaClass(HTMLFrameElement.class), "frame")
            .put(metaClass(HTMLFrameSetElement.class), "frameset")
            .put(metaClass(HTMLHeadElement.class), "head")
            .put(metaClass(HTMLHeadingElement.class), "h1")
            .put(metaClass(HTMLHeadingElement.class), "h2")
            .put(metaClass(HTMLHeadingElement.class), "h3")
            .put(metaClass(HTMLHeadingElement.class), "h4")
            .put(metaClass(HTMLHeadingElement.class), "h5")
            .put(metaClass(HTMLHeadingElement.class), "h6")
            .put(metaClass(HTMLHRElement.class), "hr")
            .put(metaClass(HTMLHtmlElement.class), "html")
            .put(metaClass(HTMLIFrameElement.class), "iframe")
            .put(metaClass(HTMLImageElement.class), "img")
            .put(metaClass(HTMLInputElement.class), "input")
            .put(metaClass(HTMLIsIndexElement.class), "isindex")
            .put(metaClass(HTMLLabelElement.class), "label")
            .put(metaClass(HTMLLegendElement.class), "legend")
            .put(metaClass(HTMLLIElement.class), "li")
            .put(metaClass(HTMLLinkElement.class), "link")
            .put(metaClass(HTMLMapElement.class), "map")
            .put(metaClass(HTMLMenuElement.class), "menu")
            .put(metaClass(HTMLMetaElement.class), "meta")
            .put(metaClass(HTMLMeterElement.class), "meter")
            .put(metaClass(HTMLModElement.class), "del")
            .put(metaClass(HTMLModElement.class), "ins")
            .put(metaClass(HTMLObjectElement.class), "object")
            .put(metaClass(HTMLOListElement.class), "ol")
            .put(metaClass(HTMLOptGroupElement.class), "optgroup")
            .put(metaClass(HTMLOptionElement.class), "option")
            .put(metaClass(HTMLOutputElement.class), "output")
            .put(metaClass(HTMLParagraphElement.class), "p")
            .put(metaClass(HTMLParamElement.class), "param")
            .put(metaClass(HTMLPreElement.class), "pre")
            .put(metaClass(HTMLProgressElement.class), "progress")
            .put(metaClass(HTMLQuoteElement.class), "blockquote")
            .put(metaClass(HTMLQuoteElement.class), "q")
            .put(metaClass(HTMLScriptElement.class), "script")
            .put(metaClass(HTMLSelectElement.class), "select")
            .put(metaClass(HTMLSourceElement.class), "source")
            .put(metaClass(HTMLStyleElement.class), "style")
            .put(metaClass(HTMLTableCaptionElement.class), "caption")
            .put(metaClass(HTMLTableCellElement.class), "td")
            .put(metaClass(HTMLTableCellElement.class), "th")
            .put(metaClass(HTMLTableColElement.class), "col")
            .put(metaClass(HTMLTableColElement.class), "colgroup")
            .put(metaClass(HTMLTableElement.class), "table")
            .put(metaClass(HTMLTableRowElement.class), "tr")
            .put(metaClass(HTMLTableSectionElement.class), "tbody")
            .put(metaClass(HTMLTableSectionElement.class), "tfoot")
            .put(metaClass(HTMLTableSectionElement.class), "thead")
            .put(metaClass(HTMLTemplateElement.class), "template")
            .put(metaClass(HTMLTextAreaElement.class), "textarea")
            .put(metaClass(HTMLTitleElement.class), "title")
            .put(metaClass(HTMLTrackElement.class), "track")
            .put(metaClass(HTMLUListElement.class), "ul")
            .put(metaClass(HTMLVideoElement.class), "video")
            .build();
  }

  private static MetaClass metaClass(final Class<?> clazz) {
    return MetaClassFactory.get(clazz);
  }
}
