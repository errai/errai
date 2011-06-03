package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface ElseBlockBuilder extends Statement {
    ElseBlockBuilder else_(Statement block);
}
