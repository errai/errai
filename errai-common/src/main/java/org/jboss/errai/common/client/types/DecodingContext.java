package org.jboss.errai.common.client.types;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class DecodingContext {
    private Map<String, Object> objects = new HashMap<String, Object>();

    public void putObject(String id, Object obj) {
        objects.put(id, obj);
    }

    
}
