package org.jboss.errai.ioc.rebind.ioc.codegen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CallParameters extends AbstractStatement {
    private List<Statement> parameters;

    public CallParameters(List<Statement> parameters) {
        this.parameters = parameters;
    }

    public static CallParameters fromStatements(Statement... statements) {
        return new CallParameters(Arrays.asList(statements));
    }

    public static CallParameters fromStatements(String... statements) {
        List<Statement> parameters = new ArrayList<Statement>(statements.length);
        for (String s : statements) {
            parameters.add(new StringStatement(s));
        }
        return new CallParameters(parameters);
    }

    public static CallParameters none() {
        return new CallParameters(Collections.<Statement>emptyList());
    }


    public String generate() {
        StringBuilder buf = new StringBuilder("(");
        for (int i = 0; i < parameters.size(); i++) {
            buf.append(parameters.get(i).generate());

            if (i + 1 < parameters.size()) {
                buf.append(", ");
            }
        }
        return buf.append(")").toString();
    }
}
