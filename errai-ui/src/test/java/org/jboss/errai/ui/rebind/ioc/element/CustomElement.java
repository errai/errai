package org.jboss.errai.ui.rebind.ioc.element;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.jboss.errai.common.client.api.annotations.Element;

class CustomElement extends HTMLDivElement {

  static class Child extends CustomElement {
  }

  @Element("foo")
  @JsType(isNative = true, namespace = JsPackage.GLOBAL)
  static class WithCustomTag extends HTMLElement {

    @Element("sub-foo")
    @JsType(isNative = true, namespace = JsPackage.GLOBAL)
    static class ChildWithCustomTag extends WithCustomTag {
    }

    static class Child extends WithCustomTag {
    }

  }

}
