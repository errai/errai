package org.jboss.errai.bus.client;

import java.util.Map;
import java.util.Set;

public interface ClientMessageBus extends MessageBus {
    public Map<String, Set<Object>> getCapturedRegistrations();
    public void unregisterAll(Map<String, Set<Object>> all);
    
    public void beginCapture();
    public void endCapture();

    public void addPostInitTask(Runnable run);
}
