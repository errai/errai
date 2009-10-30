package org.jboss.errai.bus.server.ext;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;

import java.util.Map;

public interface ErraiConfigExtension {
    public void configure(Map<Class, Provider> bindings);
}
