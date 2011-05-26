package org.jboss.errai.ioc.rebind.ioc.codegen.builder.control;

import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public abstract class AbstractBlockConditional implements Statement {
    private Statement condition;
    private Statement block;

    protected AbstractBlockConditional(Statement condition, Statement block) {
        this.condition = condition;
        this.block = block;
    }

    public Statement getCondition() {
        return condition;
    }

    public Statement getBlock() {
        return block;
    }
}
