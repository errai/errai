package org.jboss.errai.ioc.rebind.ioc.codegen;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a code block (e.g. loop body).
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class BlockStatement extends AbstractStatement {
    private List<Statement> statements = new ArrayList<Statement>();

    public BlockStatement(Statement... statements) {
        if (statements != null) {
            for (Statement statement : statements) {
                if (statement != null)
                    this.statements.add(statement);
            }
        }
    }

    public BlockStatement addStatement(Statement statement) {
        if(statement!=null)
            statements.add(statement);
        return this;
    }

    public String generate(Context context) {
        StringBuilder buf = new StringBuilder();

        for (Statement statement : statements) {
            if (buf.length() != 0)
                buf.append("\n");

            buf.append(statement.generate(context));

            if (!buf.toString().endsWith(";"))
                buf.append(";");
        }
        return buf.toString();
    }
    
    public List<Statement> getStatements() { 
        return statements;
    }
    
    public boolean isEmpty() {
        return statements.isEmpty();
    }
}
