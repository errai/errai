package org.errai.samples.helloworld.client;

import org.jboss.errai.bus.server.annotations.Remote;

@Remote
public interface FooService {
    public int add(int x, int y);
}
