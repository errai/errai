package org.jboss.errai.ioc.rebind.ioc.codegen.builder.control;

import org.jboss.errai.ioc.rebind.ioc.codegen.AbstractStatement;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;

/**
 * Foreach statement (enhanced for loop).
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ForeachLoop extends AbstractStatement {

    private Variable loopVar;
    private String collectionExpr;
    private Statement body;

    public ForeachLoop(Variable loopVar, String collectionExpr, Statement body) {
        this.loopVar = loopVar;
        this.collectionExpr = collectionExpr;
        this.body = body;
    }

    public String generate(Context context) {
        StringBuilder buf = new StringBuilder();

        buf.append("for (").append(loopVar.getType().getFullyQualifedName()).append(" ").append(loopVar.getName())
                .append(" : ").append(collectionExpr).append(") {")
                .append("\n\t").append(body.generate(context).replaceAll("\n", "\n\t"))
                .append("\n}");

        return buf.toString();
    }
}