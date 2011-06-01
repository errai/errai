package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.Builder;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface LoopBuilder extends Statement, Builder {
    LoopBodyBuilder foreach(String loopVarName);

    LoopBodyBuilder foreach(String loopVarName, MetaClass loopVarType);

    LoopBodyBuilder foreach(String loopVarName, Class<?> loopVarType);
}
