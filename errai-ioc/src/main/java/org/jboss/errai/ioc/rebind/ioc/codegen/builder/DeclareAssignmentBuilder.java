package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.AssignmentOperator;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.VariableReference;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class DeclareAssignmentBuilder extends AssignmentBuilder {
    public DeclareAssignmentBuilder(VariableReference reference, Statement statement) {
        super(AssignmentOperator.Assignment, reference, statement);
    }

    @Override
    public String generate(Context context) {
        if (statement != null) {
            return reference.getType().getFullyQualifedName() + " " + super.generate(context);
        } else {
            return reference.getType().getFullyQualifedName() + " " + reference.getName();
        }
    }
}
