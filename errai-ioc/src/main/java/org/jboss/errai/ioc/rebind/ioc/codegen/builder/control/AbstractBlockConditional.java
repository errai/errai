package org.jboss.errai.ioc.rebind.ioc.codegen.builder.control;

import org.jboss.errai.ioc.rebind.ioc.codegen.BlockStatement;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public abstract class AbstractBlockConditional implements Statement {
    private Statement condition;
    private BlockStatement block;

    protected AbstractBlockConditional(Statement condition) {
        this.condition = condition;
        this.block = new BlockStatement();
    }
    
    protected AbstractBlockConditional(Statement condition, Statement block) {
        this.condition = condition;
        this.block = new BlockStatement(block);
    }

    public Statement getCondition() {
        return condition;
    }

    public BlockStatement getBlock() {
        return block;
    }
    
    public MetaClass getType() {
        return MetaClassFactory.get(void.class);
    }
    
    public Context getContext() {
        return null;
    }
}
