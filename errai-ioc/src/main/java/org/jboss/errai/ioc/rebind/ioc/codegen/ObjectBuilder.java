package org.jboss.errai.ioc.rebind.ioc.codegen;


import com.google.gwt.core.ext.typeinfo.JClassType;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.GWTClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.JavaReflectionClass;

public class ObjectBuilder extends AbstractStatement {
    StringBuilder buf = new StringBuilder();

    private static final int CONSTRUCT_STATEMENT_COMPLETE = 1;
    private static final int SUBCLASSED = 2;
    private static final int FINISHED = 3;

    private MetaClass type;
    private int buildState;

    private ObjectBuilder(MetaClass type) {
        this.type = type;
    }

    public static ObjectBuilder newInstanceOf(Class type) {
        return new ObjectBuilder(new JavaReflectionClass(type)).newInstance();
    }

    public static ObjectBuilder newInstanceOf(JClassType type) {
        return new ObjectBuilder(new GWTClass(type)).newInstance();
    }
    
    public static ObjectBuilder newInstanceOf(JavaReflectionClass type) {
        return new ObjectBuilder(type).newInstance();
    }

    public static ObjectBuilder newInstanceOf(GWTClass type) {
        return new ObjectBuilder(type).newInstance();
    }

    private ObjectBuilder newInstance() {
        buf.append("new ").append(type.getFullyQualifedName());
        return this;
    }

    public ObjectBuilder withParameters(CallParameters parameters) {
        buf.append(parameters.getStatement()).append(";");
        buildState |= CONSTRUCT_STATEMENT_COMPLETE;
        return this;
    }

    public ClassStructureBuilder extend() {
        return new ClassStructureBuilder(type, this);
    }

    public void integrateClassStructure(ClassStructureBuilder builder) {
        finishConstructIfNecessary();
        buf.append(" {\n").append(builder.getStatement()).append("\n}\n");
    }

    private void finishConstructIfNecessary() {
        if ((buildState & CONSTRUCT_STATEMENT_COMPLETE) == 0) {
            withParameters(CallParameters.none());
        }
    }

    public MetaClass getType() {
        return type;
    }

    public String getStatement() {
        finishConstructIfNecessary();
        return buf.toString();
    }
}
