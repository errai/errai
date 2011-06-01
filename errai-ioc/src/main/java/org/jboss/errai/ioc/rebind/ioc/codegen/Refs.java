package org.jboss.errai.ioc.rebind.ioc.codegen;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public abstract class Refs {
    public static VariableReference get(final String name) {
        return new VariableReference() {
            public String getName() {
                return name;
            }

            public String generate(Context context) {
                return getName();
            }

            public MetaClass getType() {
                return null;
            }

            public Context getContext() {
                return null;
            }

            public Statement getValue() {
                return null;
            }
        };
    }
}
