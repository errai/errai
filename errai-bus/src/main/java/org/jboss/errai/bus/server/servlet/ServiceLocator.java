package org.jboss.errai.bus.server.servlet;

import org.jboss.errai.bus.server.service.ErraiService;

public interface ServiceLocator {
    ErraiService locateService();
}
