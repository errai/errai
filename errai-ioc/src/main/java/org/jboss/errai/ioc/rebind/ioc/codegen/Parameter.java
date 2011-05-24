package org.jboss.errai.ioc.rebind.ioc.codegen;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

public class Parameter extends AbstractStatement {
    private MetaClass type;
    private String name;

    public Parameter(MetaClass type, String name) {
        this.type = type;
        this.name = name;
    }

    public static Parameter of(MetaClass type, String name) {
        return new Parameter(type, name);
    }

    public String generate() {
        return type.getFullyQualifedName() + " " + name;
    }
}
