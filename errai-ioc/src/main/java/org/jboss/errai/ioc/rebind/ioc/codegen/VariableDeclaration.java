package org.jboss.errai.ioc.rebind.ioc.codegen;

import org.jboss.errai.ioc.rebind.ioc.codegen.exception.InvalidTypeException;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * Represents a variable declaration statement.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class VariableDeclaration extends AbstractStatement {
    private Variable variable;
    private Statement initialization;

    public VariableDeclaration(Variable variable) {
        this.variable = variable;
    }

    public void initialize(Statement initialization) {
        this.initialization = initialization;
    }

    public String generate() {
        StringBuilder buf = new StringBuilder();

        MetaClass inferredType = (initialization!=null)?initialization.getType():null;
        if (variable.getType()==null) {
            if (inferredType==null) {
                throw new InvalidTypeException("No type and no initialization specified to infer the type.");
            } else {
                variable = Variable.get(variable.getName(), initialization.getType());
            }
        } 
        // use mvel instead and try to convert types
        GenUtil.assertAssignableTypes(inferredType, variable.getType());
        
        buf.append(variable.getType().getFullyQualifedName()).append(" ").append(variable.generate());
        if (initialization != null)
            buf.append(" = ").append(initialization.generate());
        buf.append(";");

        return buf.toString();
    }
}