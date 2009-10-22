package org.jboss.errai.bus.server.json;

import java.util.Map;

public class JSONUtil {
    public static Map<String, Object> decodeToMap(String in) {
        Map m = (Map<String, Object>) new JSONDecoder(in).parse();

        if (m.containsKey("__MarshalledTypes")) {
            System.out.println("WOOO!");
        }

        return m;
    }
}
