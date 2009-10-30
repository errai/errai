package org.jboss.errai.bus.server.service;

import com.google.inject.Provider;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface ErraiServiceConfigurator {
    public void configure();

    public List<File> getConfigurationRoots();
    public Map<String, Provider> getResourceProviders();
    public <T> T getResource(Class<? extends T> resourceClass);

    public boolean hasProperty(String key);
    public String getProperty(String key);
}
