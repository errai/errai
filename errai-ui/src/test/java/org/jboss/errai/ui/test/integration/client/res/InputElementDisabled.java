package org.jboss.errai.ui.test.integration.client.res;

import jsinterop.annotations.JsProperty;
import org.jboss.errai.common.client.api.annotations.ClassNames;
import org.jboss.errai.common.client.api.annotations.Element;
import org.jboss.errai.common.client.api.annotations.Properties;
import org.jboss.errai.common.client.api.annotations.Property;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * @author Dmitrii Tikhomirov <chani@me.com>
 *         Created by treblereel on 11/21/17.
 */
@Element("input")
@Properties({
        @Property(name = "disabled"),
        @Property(name = "selected"),
        @Property(name = "value",value = "InputElementDisabled")
})
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "HTMLInputElement")
public interface InputElementDisabled {

    @JsProperty
    boolean getDisabled();

    @JsProperty
    boolean getSelected();

    @JsProperty
    boolean getValue();

}
