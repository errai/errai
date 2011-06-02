package org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl;

import org.jboss.errai.ioc.rebind.ioc.codegen.AssignmentOperator;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.VariableReference;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class AssignmentBuilder implements Statement {
    protected AssignmentOperator operator;
    protected VariableReference reference;
    protected Statement statement;

    public AssignmentBuilder(AssignmentOperator operator, VariableReference reference, Statement statement) {
        this.operator = operator;
        this.reference = reference;
        this.statement = statement;
    }

    public String generate(Context context) {
        return operator.generate(reference, statement);
    }

    public MetaClass getType() {
        return reference.getType();
    }

    public Context getContext() {
        return reference.getContext();
    }
}
