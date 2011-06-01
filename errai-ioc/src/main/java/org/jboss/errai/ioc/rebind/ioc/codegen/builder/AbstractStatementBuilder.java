package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * Base class of all {@link StatementBuilder}s
 * 
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
       if (statement!=null) {
         return statement.generate();
       } 
       return "";
    }
    
    public MetaClass getType() {
        if (statement!=null) {
            return statement.getType();
        }
        return MetaClassFactory.get(Void.class);
    }

    public String toJavaString() {
        //TODO generate(context)
        return generate();
    }
}
