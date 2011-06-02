package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.Builder;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.BlockBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface LoopBuilder extends Statement, Builder {
    BlockBuilder<LoopBuilder> foreach(String loopVarName);

    BlockBuilder<LoopBuilder> foreach(String loopVarName, MetaClass loopVarType);

    BlockBuilder<LoopBuilder> foreach(String loopVarName, Class<?> loopVarType);
}
