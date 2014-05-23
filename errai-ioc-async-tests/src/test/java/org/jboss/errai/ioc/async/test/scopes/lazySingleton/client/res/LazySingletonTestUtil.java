package org.jboss.errai.ioc.async.test.scopes.lazySingleton.client.res;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LazySingletonTestUtil {
    private static final List<String> order = new ArrayList<String>();
    
    public static void reset() {
        order.clear();
    }
    
    public static void record(final String fired) {
        order.add(fired);
    }
    
    public static List<String> getOrderOfCreation() {
        return Collections.unmodifiableList(order);
    }
    
    
}
