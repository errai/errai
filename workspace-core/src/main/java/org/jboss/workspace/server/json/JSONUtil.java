package org.jboss.workspace.server.json;

import java.util.Map;
import java.util.Collection;

public class JSONUtil {
    public static Map decodeToMap(String in) {
        return (Map) new JSONDecoder(in).parse();
    }
}
