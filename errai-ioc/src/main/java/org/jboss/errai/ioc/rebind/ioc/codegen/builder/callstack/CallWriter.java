package org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class CallWriter {
    private StringBuilder buffer;

    public CallWriter() {
        reset();
    }

    public CallWriter append(String str) {
        buffer.append(str);
        return this;
    }

    public void reset() {
        buffer = new StringBuilder();
    }

    public String getCallString() {
        return buffer.toString();
    }
}
