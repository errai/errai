package org.errai.samples.helloworld.client;

import org.jboss.errai.bus.server.annotations.Remote;

@Remote
public interface MyRemote {
    public long addTwoNumbers(long x, long y);
}
