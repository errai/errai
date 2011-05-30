package org.jboss.errai.ioc.rebind.ioc.codegen;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public abstract class Refs {
    public static Reference get(final String name) {
        return new Reference() {
            public String getName() {
                return name;
            }
        };
    }
}
