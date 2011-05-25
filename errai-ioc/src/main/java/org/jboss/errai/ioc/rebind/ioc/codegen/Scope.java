package org.jboss.errai.ioc.rebind.ioc.codegen;

import java.util.LinkedList;

/**
 * This class represents a scope for variables used by {@link Statement}s. 
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class Scope {
    protected LinkedList<Variable> stack = new LinkedList<Variable>();

    public void pushVariable(Variable var) {
        stack.addFirst(var);
    }
    
    public Variable peekVariable() {
        return stack.peekFirst();
    }
    
    public Variable getVariable(String name) {
        Variable found = null;
        for (Variable var : stack) {
            if (var.getName().equals(name))
                found = var;
        }
        return found;
    }
    
    public boolean containsVariable(String var) {
        return getVariable(var)!=null;
    }
    
    public boolean containsVariable(Variable var) {
        return stack.indexOf(var)!=-1;
    }
    
    public void merge(Scope scope) {
        this.stack.addAll(stack.size(), scope.stack);
    }
}
