package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface ContextualStatement extends Statement {
    LoopBuilder.LoopBodyBuilder foreach(String loopVarName);

    LoopBuilder.LoopBodyBuilder foreach(String loopVarName, Class<?> loopVarType);

    LoopBuilder.LoopBodyBuilder foreach(String loopVarName, MetaClass loopVarType);

    ContextualStatement invoke(String methodName, Object... parameters);
}
