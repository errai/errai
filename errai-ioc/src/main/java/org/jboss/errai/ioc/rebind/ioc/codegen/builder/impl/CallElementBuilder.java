package org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl;

import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.CallElement;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class CallElementBuilder {
    protected CallElement rootElement;
    protected CallElement callElement;

    public void appendCallElement(CallElement element) {
        if (callElement == null) {
            rootElement = callElement = element;
        } else {
            callElement.setNext(element);
            callElement = element;
        }
    }

    public CallElement getRootElement() {
        return rootElement;
    }

    public CallElement getCallElement() {
        return callElement;
    }
}
