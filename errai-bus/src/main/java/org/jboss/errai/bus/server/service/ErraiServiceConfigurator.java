package org.jboss.errai.bus.server.service;

import java.io.File;
import java.util.List;

public interface ErraiServiceConfigurator {
    public void configure();
    public List<File> getConfigurationRoots();
}
