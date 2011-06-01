package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class BlockBuilder implements Statement {
    private Context context;
    private List<Statement> statements;

    public BlockBuilder(Context context) {
        this.context = context;
        this.statements = new ArrayList<Statement>();
    }

    public BlockBuilder append(Statement statement) {
        statements.add(statement);
        return this;
    }

    public String generate() {
        StringBuilder builder = new StringBuilder();
        for (Statement statement : statements) {
            builder.append(statement.generate()).append(";\n");
        }
        return builder.toString();
    }

    public MetaClass getType() {
        return null;
    }

    public Context getContext() {
        return null;
    }
}
