package org.jboss.errai.ioc.rebind.ioc.codegen;

import java.util.LinkedList;

/**
 * This class represents a scope for {@link Statement}s. 
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class Scope {
    protected LinkedList<Statement> stack = new LinkedList<Statement>();

    public void push(Statement var) {
        stack.addFirst(var);
    }
    
    public Statement peek() {
        return stack.peekFirst();
    }
    
    public boolean contains(Statement var) {
        return stack.indexOf(var)!=-1;
    }
    
    public void merge(Scope scope) {
        this.stack.addAll(stack.size(), scope.stack);
    }
}
