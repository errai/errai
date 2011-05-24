package org.jboss.errai.ioc.rebind.ioc.codegen.builder;


import org.jboss.errai.ioc.rebind.ioc.codegen.DefParameters;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;

public class ClassStructureBuilder {
    private MetaClass toExtend;
    private StringBuilder buf = new StringBuilder();

    ClassStructureBuilder(MetaClass toExtend) {
        this.toExtend = toExtend;
    }

    public ClassStructureBuilder publicConstructor(DefParameters parameters, Statement body) {
        buf.append("public ").append(toExtend.getFullyQualifedName()).append(parameters.generate()).append(" {\n");
        if (body != null) {
            buf.append(body.generate()).append("\n");
        }
        buf.append("}\n");
        return this;
    }

    public ClassStructureBuilder publicOverridesMethod(MetaMethod method, Statement body) {
        buf.append("public ").append(method.getReturnType().getFullyQualifedName()).append(" ").append(method.getName())
                .append(DefParameters.from(method).generate()).append(" {\n");
        if (body != null) {
            buf.append(body.generate()).append("\n");
        }
        buf.append("}\n");
        return this;
    }

    public String getStatement() {
        return buf.toString();
    }
}
