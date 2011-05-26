package org.jboss.errai.ioc.rebind.ioc.codegen.builder.control;

import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class DoWhileBlock extends AbstractBlockConditional {
    public DoWhileBlock(Statement condition, Statement block) {
        super(condition, block);
    }

    public String generate() {
        StringBuilder builder = new StringBuilder("do {\n");

        if (getBlock() != null) {
            builder.append(getBlock().generate());
        }

        builder.append("} while (").append(getBlock().generate()).append(");\n");

        return builder.toString();
    }
}
