package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class BooleanStatementBuilder implements Statement {
    private Statement lhs;
    private Statement rhs;
    private BooleanOperator operator;

    public BooleanStatementBuilder(Statement lhs, Statement rhs, BooleanOperator operator) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.operator = operator;
    }

    public String generate() {
        return lhs.generate() + " " + operator.getCanonicalString() + " " + rhs.generate();
    }

    public Context getContext() {
        return null;
    }

    public MetaClass getType() {
        return MetaClassFactory.get(boolean.class);
    }
    
}
