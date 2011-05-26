package org.jboss.errai.ioc.rebind.ioc.codegen;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class BlockStatement extends AbstractStatement  {
    private List<Statement> statements = new ArrayList<Statement>();
    
    public BlockStatement addStatement(Statement statement) {
        statements.add(statement);
        return this;
    }
    
    public String generate() {
        StringBuilder buf = new StringBuilder();

        for (Statement statement : statements) {
            if(buf.length()!=0)
                buf.append("\n");
            
            buf.append(statement.generate()).append(";");
        }
        return buf.toString();
    }
}
