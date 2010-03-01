package org.errai.samples.queryservice.client;

import org.jboss.errai.bus.server.annotations.Remote;

@Remote
public interface QueryServiceRemote {
    public String[] query(String queryString);
}
