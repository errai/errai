package org.jboss.errai.ioc.rebind.ioc.codegen;

import java.util.HashMap;
import java.util.Map;

import org.jboss.errai.ioc.rebind.ioc.codegen.exception.OutOfScopeException;

/**
 * This class represents a {@link Statement} context. It has a reference to its
 * parent context and holds a map of variables that are currently in scope.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class Context {
    private Map<String, Variable> variables = new HashMap<String, Variable>();
    private Context parent = null;
    private Statement statement = null;

    private Context() {
    }

    private Context(Context parent) {
        this.parent = parent;
    }

    public static Context create() {
        return new Context();
    }

    public static Context create(Context parent) {
        return new Context(parent);
    }

    public Context add(Variable variable) {
        variables.put(variable.getName(), variable);
        return this;
    }

    public Statement getParentStatement() {
        if (this.parent != null)
            return parent.statement;

        return null;
    }

    public Statement getStatement() {
        return statement;
    }

    public void setStatement(Statement statement) {
        this.statement = statement;
    }

    public Variable getVariable(String name) {
        Variable found = variables.get(name);

        Context parent = this.parent;
        while (found == null && parent != null) {
            found = parent.variables.get(name);
            parent = parent.parent;
        }
        if (found == null)
            throw new OutOfScopeException(name);

        return found;
    }
}
