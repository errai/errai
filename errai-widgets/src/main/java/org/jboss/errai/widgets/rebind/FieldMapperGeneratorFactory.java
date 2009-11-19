package org.jboss.errai.widgets.rebind;

import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import org.jboss.errai.widgets.client.WSGrid;
import org.jboss.errai.widgets.rebind.collectionmappers.WSGridFMGenerator;
import org.jboss.errai.widgets.rebind.widgetmappers.CheckBoxFMGenerator;
import org.jboss.errai.widgets.rebind.widgetmappers.CollectionFMGenerator;
import org.jboss.errai.widgets.rebind.widgetmappers.PasswordTextBoxFMGenerator;
import org.jboss.errai.widgets.rebind.widgetmappers.TextBoxFMGenerator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FieldMapperGeneratorFactory {
    private static final Map<String, FieldMapperGenerator> FIELD_MAPPERS =
            new HashMap<String, FieldMapperGenerator>();


    public static void addFieldMapper(Class widgetType, FieldMapperGenerator mapper) {
        addFieldMapper(widgetType.getName(), mapper);
    }

    private static void addFieldMapper(String widgetType, FieldMapperGenerator mapper) {
        FIELD_MAPPERS.put(widgetType, mapper);
    }

    public static FieldMapperGenerator getFieldMapper(String widgetType) {
        FieldMapperGenerator fm = FIELD_MAPPERS.get(widgetType);
        Class widgetClass = null;

        try {
            widgetClass = Class.forName(widgetType);
        }
        catch (Throwable e) {
        //    throw new RuntimeException("could not find mapper for: " + widgetType, e);
        }

        if (fm == null && widgetClass != null) {
            Class t;
            for (String className : FIELD_MAPPERS.keySet()) {
                try {
                    t = Class.forName(className);
                }
                catch (Throwable e) {
                    continue;
                }
                if (t.isAssignableFrom(widgetClass)) {
                    fm = getFieldMapper(t.getName());
                    addFieldMapper(widgetType, fm);
                    break;
                }
            }
        }
        return fm;
    }

    static {
        addFieldMapper(WSGrid.class, new WSGridFMGenerator());

        addFieldMapper(TextBox.class, new TextBoxFMGenerator());
        addFieldMapper(PasswordTextBox.class, new PasswordTextBoxFMGenerator());
        addFieldMapper(CheckBoxFMGenerator.class, new CheckBoxFMGenerator());

        addFieldMapper(Collection.class, new CollectionFMGenerator());
    }
}
