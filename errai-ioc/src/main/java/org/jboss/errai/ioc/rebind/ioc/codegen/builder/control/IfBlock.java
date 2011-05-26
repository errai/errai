package org.jboss.errai.ioc.rebind.ioc.codegen.builder.control;

import org.jboss.errai.ioc.rebind.ioc.codegen.Scope;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class IfBlock extends AbstractBlockConditional {
    protected IfBlock elseIf;

    public IfBlock(Statement condition, Statement block) {
        super(condition, block);
    }

    public IfBlock(Statement condition, Statement block, IfBlock elseIf) {
        super(condition, block);
        this.elseIf = elseIf;
    }

    public String generate() {
        StringBuilder builder = new StringBuilder("if ");
        if (getCondition() != null) {
            builder.append("(").append(getCondition().generate()).append(") ");
        }
        builder.append("{\n");

        if (getBlock() != null) {
            builder.append(getBlock().generate());
        }

        builder.append("\n} ");

        if (elseIf != null) {
            builder.append("else ").append(elseIf.generate());
        }

        return builder.toString();
    }

    public Scope getScope() {
        return null;
    }
}
