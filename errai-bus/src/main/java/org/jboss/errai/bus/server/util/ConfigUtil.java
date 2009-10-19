package org.jboss.errai.bus.server.util;

import java.io.File;
import static java.lang.Thread.currentThread;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

public class ConfigUtil {
    public static final String ERRAI_CONFIG_STUB_NAME = "ErraiApp.properties";

    public static List<File> findAllConfigTargets() {
        try {
            Enumeration<URL> t = currentThread().getContextClassLoader().getResources(ERRAI_CONFIG_STUB_NAME);
            List<File> targets = new LinkedList<File>();
            while (t.hasMoreElements()) {
                targets.add(new File(t.nextElement().getFile()).getParentFile());
            }

            return targets;
        }
        catch (Exception e) {
            throw new RuntimeException("Could not generate extension proxies", e);
        }
    }
}
