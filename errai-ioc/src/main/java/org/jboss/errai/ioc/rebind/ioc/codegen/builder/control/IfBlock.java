package org.jboss.errai.ioc.rebind.ioc.codegen.builder.control;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.BooleanExpressionBuilder;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class IfBlock extends AbstractBlockConditional {
    private Statement elseBlock;
    private IfBlock elseIf;
  
    public IfBlock(BooleanExpressionBuilder condition, Statement block) {
        super(condition, block);
    }
    
    public IfBlock(BooleanExpressionBuilder condition, Statement block, IfBlock elseIf) {
        super(condition, block);
        this.elseIf = elseIf;
    }

    public BooleanExpressionBuilder getCondition() {
        return (BooleanExpressionBuilder) super.getCondition();
    }
    
    public void setElseBlock(Statement elseBlock) {
        this.elseBlock = elseBlock;
    }

    public void setElseIf(IfBlock elseIf) {
        this.elseIf = elseIf;
    }

    public String generate(Context context) {
        StringBuilder builder = new StringBuilder("if ");
        builder.append("(").append(getCondition().generate(context)).append(") ");
        
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
