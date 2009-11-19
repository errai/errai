package org.jboss.errai.widgets.client.mapping;

import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.List;

public class WidgetMapper<T extends Widget, V> {
    protected List<MapperChangeHandler<?>> changeHandlers;
    protected FieldMapper[] fields;
    protected T widget;
    protected String[] defaultTitleValues;

    public void map(V entity) {
        for (FieldMapper<T, T, V> m : fields) {
            m.getFieldValue(widget, entity);
        }
    }

    public void setFields(FieldMapper[] fields) {
        this.fields = fields;
    }

    public String[] getDefaultTitleValues() {
        return defaultTitleValues;
    }

    public void setDefaultTitleValues(String[] defaultTitleValues) {
        this.defaultTitleValues = defaultTitleValues;
    }

    public void addMapperChangeHandler(MapperChangeHandler<?> handler) {
        if (changeHandlers == null) changeHandlers = new ArrayList<MapperChangeHandler<?>>();
        changeHandlers.add(handler);
    }

    @SuppressWarnings({"unchecked"})
    protected void fireAllChangeHandlers(Object o) {
        if (changeHandlers == null) return;

        for (MapperChangeHandler handler : changeHandlers) {
            handler.onChange(o);
        }
    }
}
