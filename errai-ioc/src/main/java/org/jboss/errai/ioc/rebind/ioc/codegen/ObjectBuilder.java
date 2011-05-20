package org.jboss.errai.ioc.rebind.ioc.codegen;


import com.google.gwt.core.ext.typeinfo.JClassType;

public class ObjectBuilder implements Statement {
    StringBuilder buf = new StringBuilder();

    private static final int CONSTRUCT_STATEMENT_COMPLETE = 1;
    private static final int SUBCLASSED = 2;
    private static final int FINISHED = 3;

    private JClassType type;
    private int buildState;

    private ObjectBuilder(JClassType type) {
        this.type = type;
    }

    public static ObjectBuilder newInstanceOf(JClassType type) {
       return new ObjectBuilder(type).newInstance();
    }

    private ObjectBuilder newInstance() {
        buf.append("new ").append(type.getQualifiedSourceName());
        return this;
    }

    public ObjectBuilder withParameters(CallParameters parameters) {
        buf.append(parameters.getStatement());
        buildState |= CONSTRUCT_STATEMENT_COMPLETE;
        return this;
    }

    public ClassStructureBuilder extend() {
        return new ClassStructureBuilder(type, this);
    }

    private void finishConstructIfNecessary() {
        if ((buildState & CONSTRUCT_STATEMENT_COMPLETE) != 0) {
            withParameters(CallParameters.none());
        }
    }

    public String getStatement() {
        finishConstructIfNecessary();
        return buf.toString();
    }
}
