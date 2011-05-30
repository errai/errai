package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.VariableReference;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class AssignmentBuilder implements Statement {
    protected VariableReference reference;
    protected Statement statement;

    public AssignmentBuilder(VariableReference reference, Statement statement) {
        this.reference = reference;
        this.statement = statement;
    }

    public String generate() {
        return reference.generate() + " = " + statement.generate();
    }

    public MetaClass getType() {
        return null;
    }

    public Context getContext() {
        return null;
    }
}
