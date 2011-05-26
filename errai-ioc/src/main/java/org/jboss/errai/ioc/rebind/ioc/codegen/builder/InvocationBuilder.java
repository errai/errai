package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.CallParameters;
import org.jboss.errai.ioc.rebind.ioc.codegen.HasScope;
import org.jboss.errai.ioc.rebind.ioc.codegen.Scope;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.UndefinedMethodException;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;

/**
 * StatementBuilder to generate method invocations.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class InvocationBuilder extends AbstractStatementBuilder implements Statement {

    private Variable var;
    private MetaMethod method;
    private CallParameters parameters;
    
    protected InvocationBuilder(Scope scope) {
        super(scope);
    }

    public static InvocationBuilder createInScopeOf(HasScope parent) {
        return new InvocationBuilder(parent.getScope());
    }

    public InvocationBuilder invoke(String methodName, Variable... parameters) {
        this.var = scope.peekVariable();
       
        MetaClass[] parameterTypes = new MetaClass[parameters.length];
        for (int i=0; i<parameters.length; i++) {
            parameterTypes[i] = parameters[i].getType();
        }
        
        this.method = var.getType().getDeclaredMethod(methodName, parameterTypes);
        if (method == null) 
            throw new UndefinedMethodException(methodName, parameterTypes);

        this.parameters = CallParameters.fromStatements(parameters);
        return this;
    }
    
    public String generate() {
        StringBuilder buf = new StringBuilder();
        buf.append(var.getName()).append(".").append(method.getName()).append(parameters.generate());
        return buf.toString();
    }
}