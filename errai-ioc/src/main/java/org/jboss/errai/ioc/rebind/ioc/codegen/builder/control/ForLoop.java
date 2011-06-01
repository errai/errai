package org.jboss.errai.ioc.rebind.ioc.codegen.builder.control;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class ForLoop extends AbstractBlockConditional {
    private Statement initializer;
    private Statement afterBlock;

    public ForLoop(Statement condition, Statement block) {
        super(condition, block);
    }

    public ForLoop(Statement condition, Statement block, Statement initializer, Statement afterBlock) {
        super(condition, block);
        this.initializer = initializer;
        this.afterBlock = afterBlock;
    }

    public String generate(Context context) {
        StringBuilder builder = new StringBuilder("for (");

        if (initializer != null) {
            builder.append(initializer.generate(context));
        }
        builder.append("; ").append(getCondition().generate(context)).append("; ");

        if (afterBlock != null) {
            builder.append(afterBlock.generate(context));
        }

        builder.append(") {\n")
                .append(getBlock().generate(context))
                .append("\n}\n");

        return builder.toString();
    }
}
