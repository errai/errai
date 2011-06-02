package org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.GenUtil;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class DynamicLoad extends AbstractCallElement {
    private Object value;

    public DynamicLoad(Object value) {
        this.value = value;
    }

    public String getStatement(Context context, Statement statement) {
        return nextOrReturn(context, GenUtil.generate(context, value));
    }
}
