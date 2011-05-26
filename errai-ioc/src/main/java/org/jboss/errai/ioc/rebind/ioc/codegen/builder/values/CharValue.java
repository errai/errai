package org.jboss.errai.ioc.rebind.ioc.codegen.builder.values;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class CharValue extends LiteralValue<Character> {

    public CharValue(Character value) {
        super(value);
    }

    @Override
    public String getCanonicalString() {
        return getValue().toString();
    }
}
