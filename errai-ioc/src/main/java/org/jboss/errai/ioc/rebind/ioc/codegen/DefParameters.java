package org.jboss.errai.ioc.rebind.ioc.codegen;

import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameter;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaParameter;

import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DefParameters implements Statement {
    private List<Parameter> parameters;

    public DefParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public static DefParameters from(MetaMethod method) {
        List<Parameter> parameters = new ArrayList<Parameter>();
        for (MetaParameter parm : method.getParameters()) {
            parameters.add(Parameter.of(parm.getType(), parm.getName()));
        }
        return new DefParameters(parameters);
    }

    public static DefParameters fromStatements(Parameter... statements) {
        return new DefParameters(Arrays.asList(statements));
    }

    public static CallParameters none() {
        return new CallParameters(Collections.<Statement>emptyList());
    }


    public String getStatement() {
        StringBuilder buf = new StringBuilder("(");
        for (int i = 0; i < parameters.size(); i++) {
            buf.append(parameters.get(i).getStatement());

            if (i + 1 < parameters.size()) {
                buf.append(", ");
            }
        }
        return buf.append(")").toString();
    }
}
