package org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class StaticLoad extends AbstractCallElement {
    private final Class<?> type;

    public StaticLoad(Class<?> type) {
        this.type = type;
    }

    public void handleCall(CallWriter writer, Context context, Statement statement) {
        final MetaClass metaClass = MetaClassFactory.get(type);
        statement = new Statement() {
            public String generate(Context context) {
                return metaClass.getFullyQualifedName();
            }

            public MetaClass getType() {
                return metaClass;
            }

            public Context getContext() {
                return null;
            }
        };
        
        nextOrReturn(writer, context, statement);
    }
}
