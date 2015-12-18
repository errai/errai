package org.jboss.errai.processor.testcase;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.Model;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

@Dependent
public class BoundNoWarnings {
    @Inject @Model
    private BoundModelClass model;

    @Bound
    private final Label property1 = new Label();

    @Inject @Bound
    private TextBox property2;

    @Bound
    private Element property3 = DOM.createDiv();
    
    @Inject @Bound(property="property4")
    private JsTypeInputElement thisNameIsOverriddenByAttribute;

    @Inject @Bound(property="property6.property1")
    private TextBox propertyChainOneLevel;
    
    @Inject @Bound(property="property6.property6.property1")
    private TextBox propertyChainTwoLevels;
    
    private final Widget constructorInjectedWidget;
    private final TextBox widgetNamedByGetter = new TextBox();
    private TextBox methodInjectedWidget = new TextBox();

    @Inject
    public BoundNoWarnings(@Bound Widget property5) {
        this.constructorInjectedWidget = property5;
    }

    @Inject
    public void thisNameDoesntMatter(@Bound TextBox property3) {
        this.methodInjectedWidget = property3;
    }

    @Bound
    public TextBox getProperty4() {
        return widgetNamedByGetter;
    }
}