package org.jboss.errai.ioc.rebind.ioc.codegen;


import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;

public class ClassStructureBuilder {
    private JClassType toExtend;
    private ObjectBuilder objectBuilder;
    private StringBuilder buf = new StringBuilder();

    ClassStructureBuilder(JClassType toExtend, ObjectBuilder objectBuilder) {
        this.toExtend = toExtend;
        this.objectBuilder = objectBuilder;
    }

    public ClassStructureBuilder publicConstructor(DefParameters parameters, Statement body) {
        buf.append("public ").append(toExtend.getQualifiedSourceName()).append(parameters.getStatement()).append(" {\n");
        if (body != null) {
            buf.append(body.getStatement()).append("\n");
        }
        buf.append("}\n");
        return this;
    }

    public ClassStructureBuilder publicOverridesMethod(JMethod method, Statement body) {
        buf.append("public ").append(method.getReturnType()).append(" ").append(method.getName())
                .append(DefParameters.from(method).getStatement()).append(" {\n");
        if (body != null) {
            buf.append(body.getStatement()).append("\n");
        }
        buf.append("}\n");
        return this;
    }

    public String getStatement() {
        return buf.toString();
    }
}
