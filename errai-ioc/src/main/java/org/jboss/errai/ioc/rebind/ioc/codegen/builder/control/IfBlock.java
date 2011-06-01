package org.jboss.errai.ioc.rebind.ioc.codegen.builder.control;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class IfBlock extends AbstractBlockConditional {
    protected Statement elseBlock;
    protected IfBlock elseIf;

    public IfBlock(Statement condition, Statement block) {
        super(condition, block);
    }

    public IfBlock(Statement condition, Statement block, IfBlock elseIf) {
        super(condition, block);
        this.elseIf = elseIf;
    }

    public void setElseBlock(Statement elseBlock) {
        this.elseBlock = elseBlock;
    }

    public String generate(Context context) {
        StringBuilder builder = new StringBuilder("if ");
        if (getCondition() != null) {
            builder.append("(").append(getCondition().generate(context)).append(") ");
        }
        builder.append("{\n");

        if (getBlock() != null) {
            builder.append(getBlock().generate(context));
        }

        builder.append("\n} ");

        if (elseIf != null) {
            builder.append("else ").append(elseIf.generate(context));
            return builder.toString();
        }

        if (elseBlock != null) {
            builder.append("else { ").append(elseBlock.generate(context)).append("\n} ");
            return builder.toString();
        }

        return builder.toString();
    }
}
