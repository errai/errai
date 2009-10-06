package org.jboss.errai.bus.server.json;

import java.util.Map;

public class JSONUtil {
    public static Map<String, Object> decodeToMap(String in) {
        return (Map<String, Object>) new JSONDecoder(in).parse();
    }
   
}
