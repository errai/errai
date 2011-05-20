package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.apache.catalina.User;
import org.jboss.errai.bus.client.api.base.MessageBuilder;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class StatementBuilder {
    public static Actions create() {
        return null;
    }

    public static void main(String[] args) {
        create().newObject(null).invokeMethod("foo").invokeMethod("bar");

        for (int i = 0; i < 100; i++) {

        }




        // new Object().foo().bar();
    }
}
