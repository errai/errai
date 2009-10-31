package org.jboss.errai.widgets.client.mapping;

import com.google.gwt.user.client.ui.Widget;

public interface ColumnMapper<T extends Widget, F, V> {
    public void mapRow(int row, FieldMapper<T, F, V>[] fields, T w, V value);
}
