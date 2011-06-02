package org.jboss.errai.ioc.rebind.ioc.codegen;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface Builder {
    /**
     * Validates the statement and generates the String representation.
     * 
     * @return Java String representation
     */
    public String toJavaString();
}
