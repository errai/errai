package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.BlockStatement;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class BlockBuilder<T> implements Finishable<T> {
    private BlockStatement blockStatement;
    private BuildCallback<T> callback;

    public BlockBuilder() {
        this.blockStatement = new BlockStatement();
    }

    public BlockBuilder(BuildCallback<T> callback) {
        this.callback = callback;
    }

    public BlockBuilder<T> append(Statement statement) {
        blockStatement.addStatement(statement);
        return this;
    }

    public T finish() {
        if (callback != null) {
            return callback.callback(blockStatement);
        }
        return null;
    }
}
