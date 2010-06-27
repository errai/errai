package org.errai.samples.helloworld.server;

import org.errai.samples.helloworld.client.MyRemote;
import org.jboss.errai.bus.server.annotations.Service;

@Service
public class MyService implements MyRemote {
    public long addTwoNumbers(long x, long y) {
        return x + y;
    }
}
