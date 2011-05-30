package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.VariableReference;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class DeclareAssignmentBuilder extends AssignmentBuilder {
    public DeclareAssignmentBuilder(VariableReference reference, Statement statement) {
        super(reference, statement);
    }

    @Override
    public String generate() {
        if (statement != null) {
            return reference.getType().getFullyQualifedName() + " " + super.generate();
        } else {
            return reference.getType().getFullyQualifedName() + " " + reference.getName();
        }
    }
}
