package org.jboss.errai.ioc.rebind.ioc.codegen.builder.control;

import org.jboss.errai.ioc.rebind.ioc.codegen.BlockStatement;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.BooleanExpressionBuilder;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class IfBlock extends AbstractBlockConditional {
    private BlockStatement elseBlock = new BlockStatement();
    private IfBlock elseIfBlock;

    public IfBlock(BooleanExpressionBuilder condition) {
        super(condition);
    }

    public IfBlock(BooleanExpressionBuilder condition, Statement block) {
        super(condition, new BlockStatement(block));
    }

    public BooleanExpressionBuilder getCondition() {
        return (BooleanExpressionBuilder) super.getCondition();
    }

    public BlockStatement getElseBlock() {
        return elseBlock;
    }
    
    public IfBlock getElseIfBlock() {
        return elseIfBlock;
    }
    
    public void setElseIfBlock(IfBlock elseIfBlock) {
        this.elseIfBlock = elseIfBlock;
    }
    
    public String generate(Context context) {
        StringBuilder builder = new StringBuilder("if ");
        builder.append("(").append(getCondition().generate(context)).append(") ");

        builder.append("{\n");

        if (getBlock() != null) {
            builder.append(getBlock().generate(context));
        }

        builder.append("\n} ");

        if (elseIfBlock != null) {
            builder.append("else ").append(elseIfBlock.generate(context));
            return builder.toString();
        }
        
        if (elseBlock != null && !elseBlock.isEmpty()) {
            builder.append("else { ").append(elseBlock.generate(context)).append("\n} ");
            return builder.toString();
        }

        return builder.toString();
    }
}
