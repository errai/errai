package org.jboss.errai.widgets.client.mapping;

import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.List;

public abstract class WidgetMapper<T extends Widget, F, V> {
    protected List<MapperChangeHandler<V>> changeHandlers;
    protected FieldMapper[] fields;
    protected T widget;

    public void map(List<V> list) {
        for (V o : list) {
             for (FieldMapper<T, F, V> fm : fields) {
                 fm.getFieldValue(widget, o);
             }
        }
    }

    public void map(V obj) {
         for (FieldMapper<T, F, V> fm : fields) {
             fm.getFieldValue(widget, obj);
         }
    }

    public void setFields(FieldMapper[] fields) {
        this.fields = fields;
    }

    public void addMapperChangeHandler(MapperChangeHandler<V> handler) {
        if (changeHandlers == null) changeHandlers = new ArrayList<MapperChangeHandler<V>>();
        changeHandlers.add(handler);
    }

    protected void fireAreChangeHandlers(V o) {
        if (changeHandlers == null) return;

        for (MapperChangeHandler<V> handler : changeHandlers) {
            handler.onChange(o);
        }
    }
}
