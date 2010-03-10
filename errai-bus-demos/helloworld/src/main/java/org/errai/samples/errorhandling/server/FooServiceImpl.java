package org.errai.samples.errorhandling.server;

import org.errai.samples.errorhandling.client.FooService;
import org.jboss.errai.bus.server.annotations.Service;

@Service
public class FooServiceImpl implements FooService {
    public int add(int x, int y) {
        return x + y;
    }
}
