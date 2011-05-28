package org.jboss.errai.ioc.rebind.ioc.codegen;

import org.jboss.errai.ioc.rebind.ioc.codegen.exception.OutOfScopeException;

import javax.enterprise.util.TypeLiteral;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a {@link Statement} context. It has a reference to its
 * parent context and holds a map of variables to represent the statement's scope.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class Context {
    private Map<String, Variable> variables = new HashMap<String, Variable>();
    private Context parent = null;

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

    public Context add(String name, Class type) {
        return add(Variable.get(name, type));
    }

    public Context add(String name, TypeLiteral<?> type) {
        return add(Variable.get(name, type));
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

    public boolean isScoped(Variable variable) {
        Context ctx = this;
        do {
            if (ctx.variables.containsValue(variable)) return true;
        } while ((ctx = ctx.parent) != null);
        return false;
    }
}
