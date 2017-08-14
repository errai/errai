package org.jboss.errai.ui.test.elemental2.client.res;

import elemental2.dom.HTMLAnchorElement;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.jboss.errai.common.client.api.annotations.Element;
import org.jboss.errai.common.client.ui.HasValue;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "HTMLAnchorElement")
public class Elemental2EmailAnchor extends HTMLAnchorElement implements HasValue<String> {

  @JsOverlay
  @Override
  public final String getValue() {
    return textContent;
  }

  @JsOverlay
  @Override
  public final void setValue(final String value) {
    textContent = value;
    href = "mailto:" + value;
  }
}
