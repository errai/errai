package org.jboss.errai.widgets.client.mapping;

import com.google.gwt.user.client.ui.Widget;

public interface FieldMapper<T extends Widget, F, V> {
    public F getFieldValue(T w, V value);
}
