package org.jboss.errai.processor.testcase;

import java.util.List;

import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.Model;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class BoundToNonExistingModelProperty {
    @Inject @Model
    private BoundModelClass model;

    @Bound
    private final Label nonProperty1 = new Label();

    @Inject @Bound
    private TextBox nonProperty2;

    private final Widget constructorInjectedWidget;
    private final TextBox widgetNamedByGetter = new TextBox();
    private TextBox methodInjectedWidget = new TextBox();

    @Inject
    public BoundToNonExistingModelProperty(@Bound Widget nonProperty5) {
        this.constructorInjectedWidget = nonProperty5;
    }

    @Inject
    public void thisNameDoesntMatter(@Bound TextBox nonProperty3) {
        this.methodInjectedWidget = nonProperty3;
    }

    @Bound
    public TextBox getNonProperty4() {
        return widgetNamedByGetter;
    }
}