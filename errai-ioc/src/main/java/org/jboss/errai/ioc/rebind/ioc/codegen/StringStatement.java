package org.jboss.errai.ioc.rebind.ioc.codegen;

public class StringStatement extends AbstractStatement {
    private final String statement;

    public StringStatement(String statement) {
        this.statement = statement;
    }

    public String generate() {
        return statement;
    }
}
