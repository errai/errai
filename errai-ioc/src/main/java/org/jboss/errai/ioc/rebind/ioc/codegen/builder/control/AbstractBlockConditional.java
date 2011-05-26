package org.jboss.errai.ioc.rebind.ioc.codegen.builder.control;

import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.Scope;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

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
    
    public MetaClass getType() {
        return MetaClassFactory.get(void.class);
    }
    
    public Scope getScope() {
        return null;
    }
}
