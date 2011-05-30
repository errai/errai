package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class VariableContextualStatementBuilder extends ContextualStatementBuilder implements VariableReferenceContextualStatement {
    public VariableContextualStatementBuilder(AbstractStatementBuilder parent) {
        super(parent);
    }

    public static VariableContextualStatementBuilder createInContextOf(AbstractStatementBuilder parent) {
        return new VariableContextualStatementBuilder(parent);
    }


}
