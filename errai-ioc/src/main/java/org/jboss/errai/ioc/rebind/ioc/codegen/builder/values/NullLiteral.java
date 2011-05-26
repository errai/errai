package org.jboss.errai.ioc.rebind.ioc.codegen.builder.values;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class NullLiteral extends LiteralValue<Object> {
    public static final NullLiteral INSTANCE = new NullLiteral();

    private NullLiteral() {
        super(null);
    }

    @Override
    public String getCanonicalString() {
        return "null";
    }
}
