package org.errai.samples.queryservice.client;

import org.jboss.errai.bus.server.annotations.Multi;
import org.jboss.errai.bus.server.annotations.Remote;

@Remote
@Multi
public interface QueryServiceRemote {
    public String[] query(String queryString);
    public String append(String... arg);
}
