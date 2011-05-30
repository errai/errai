package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public abstract class AbstractStatementBuilder implements Statement {
    protected Context context = null;
    protected Statement statement = null;
    protected AbstractStatementBuilder parent = null;
    
    protected AbstractStatementBuilder(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }
    
    public String generate() {
        return statement.generate();
    }
    
    public MetaClass getType() {
        return statement.getType();
    }
}
