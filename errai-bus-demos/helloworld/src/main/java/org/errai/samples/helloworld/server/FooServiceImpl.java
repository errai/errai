package org.errai.samples.helloworld.server;

import org.errai.samples.helloworld.client.FooService;
import org.jboss.errai.bus.server.annotations.Service;

@Service
public class FooServiceImpl implements FooService {
    public int add(int x, int y) {
        return x + y;
    }
}
