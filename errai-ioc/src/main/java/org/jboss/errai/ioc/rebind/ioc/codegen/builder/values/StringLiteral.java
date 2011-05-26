package org.jboss.errai.ioc.rebind.ioc.codegen.builder.values;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class StringLiteral extends LiteralValue<String> {
    public StringLiteral(String value) {
        super(value);
    }

    @Override
    public String getCanonicalString() {
        StringBuilder builder = new StringBuilder("\"");
        for (char c : getValue().toCharArray()) {
            switch (c) {
                case '\\':
                case '"':
                    builder.append("\\").append(c);
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                case '\t':
                    builder.append("\\t");
                    break;
                default:
                    builder.append(c);
            }
        }
        return builder.append("\"").toString();
    }
}
