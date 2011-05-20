package org.jboss.errai.ioc.rebind.ioc.codegen;

import com.google.gwt.core.ext.typeinfo.JClassType;

public class Parameter implements Statement {
    private JClassType type;
    private String name;

    public Parameter(JClassType type, String name) {
        this.type = type;
        this.name = name;
    }

    public static Parameter of(JClassType type, String name) {
        return new Parameter(type, name);
    }

    public String getStatement() {
        return type.getQualifiedSourceName() + " " + name;
    }
}
