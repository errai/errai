package org.jboss.errai.ioc.rebind.ioc.codegen;

public class StringStatement implements Statement {
    private final String statement;

    public StringStatement(String statement) {
        this.statement = statement;
    }

    public String getStatement() {
        return statement;
    }
}
