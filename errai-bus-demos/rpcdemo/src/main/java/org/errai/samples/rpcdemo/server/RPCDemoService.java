package org.errai.samples.rpcdemo.server;

import com.google.inject.Inject;
import org.errai.samples.rpcdemo.client.TestService;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.server.annotations.Service;

@Service
public class RPCDemoService implements TestService {
    private MessageBus bus;

    @Inject
    public RPCDemoService(MessageBus bus) {
        this.bus = bus;
    }

    public long getMemoryFree() {
        return Runtime.getRuntime().freeMemory();
    }

    public String append(String str, String str2) {
        return str + str2;
    }

    public long add(long x, long y) {
        return x + y;
    }

    public void update(String status) {
        // check void return type
    }
}