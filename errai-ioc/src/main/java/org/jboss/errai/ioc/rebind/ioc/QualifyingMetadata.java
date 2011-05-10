package org.jboss.errai.ioc.rebind.ioc;

/**
 * @author Mike Brock
 */
public interface QualifyingMetadata {
    public boolean doesSatisfy(QualifyingMetadata metadata);
}
