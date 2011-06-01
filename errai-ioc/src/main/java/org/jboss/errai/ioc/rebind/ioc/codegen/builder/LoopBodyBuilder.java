package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface LoopBodyBuilder extends Statement {
    public LoopBodyBuilder execute(Statement statement);
}
