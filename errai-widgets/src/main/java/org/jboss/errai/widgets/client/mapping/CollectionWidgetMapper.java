package org.jboss.errai.widgets.client.mapping;

import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.List;

public abstract class CollectionWidgetMapper<T extends Widget, F, V> extends WidgetMapper<T, V> {
    @Override
    public abstract void map(V entity);
}
