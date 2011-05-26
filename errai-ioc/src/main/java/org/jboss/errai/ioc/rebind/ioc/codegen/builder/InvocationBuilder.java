package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.CallParameters;
import org.jboss.errai.ioc.rebind.ioc.codegen.HasScope;
import org.jboss.errai.ioc.rebind.ioc.codegen.Scope;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.UndefinedMethodException;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;

/**
 * StatementBuilder to generate method invocations.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class InvocationBuilder extends AbstractStatementBuilder {
    private Statement statement;
    private MetaMethod method;
    private CallParameters parameters;
    
    protected InvocationBuilder(Scope scope) {
        super(scope);
    }

    public static InvocationBuilder createInScopeOf(HasScope parent) {
        return new InvocationBuilder(parent.getScope());
    }

    public ScopedStatementBuilder invoke(String methodName, Statement... parameters) {
        this.statement = scope.peek();
        this.parameters = CallParameters.fromStatements(parameters);
        
        MetaClass[] parameterTypes = new MetaClass[parameters.length];
        for (int i=0; i<parameters.length; i++) {
            // TODO assertInScope(parameters[i]);
            parameterTypes[i] = parameters[i].getType();
        }
        
        this.method = statement.getType().getDeclaredMethod(methodName, parameterTypes);
        if (method == null) 
            throw new UndefinedMethodException(methodName, parameterTypes);

        buf.append(statement.generate()).append(".").append(method.getName()).append(this.parameters.generate());
        scope.push(this);
        return ScopedStatementBuilder.createInScopeOf(this);
    }
    
    public MetaClass getType() {
        return method.getReturnType();
    }
}