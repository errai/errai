package org.jboss.errai.ioc.rebind.ioc.codegen;

import com.google.common.collect.ArrayListMultimap;

/**
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class Scope {
    protected ArrayListMultimap<String, Variable> scope = ArrayListMultimap.<String, Variable> create();

    public void addVariable(String name, Variable var) {
        scope.put(name, var);
    }
    
    public Variable getVariable(String name) {
        Variable var = null;
        if(!scope.get(name).isEmpty())
            var = scope.get(name).get(0);
        
        return var;
    }
    
    public boolean containsVariable(Variable var) {
        return scope.containsValue(var);
    }    
}
