package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.control.IfBlock;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface ElseBlockBuilder extends Statement {
    IfBlock else_(Statement block);
}
