package org.jboss.errai.ioc.rebind.ioc.codegen.meta;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface MetaClassMember extends HasAnnotations {
    public MetaClass getDeclaringClass();

    public abstract boolean isAbstract();

    public abstract boolean isPublic();

    public abstract boolean isPrivate();

    public abstract boolean isProtected();

    public abstract boolean isFinal();

    public abstract boolean isStatic();

    public abstract boolean isTransient();

    public abstract boolean isSynthetic();

    public abstract boolean isSynchronized();
}
