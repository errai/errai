package org.jboss.errai.ioc.rebind.ioc.codegen;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface Operator {
    public String getCanonicalString();

    public int getOperatorPrecedence();

    public boolean isHigherPrecedenceThan(Operator operator);

    public boolean isEqualOrHigherPrecedenceThan(Operator operator);

    public boolean canBeApplied(MetaClass clazz);

    public void assertCanBeApplied(MetaClass clazz);
}
