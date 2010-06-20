package org.errai.samples.helloworld.server;

import org.jboss.errai.bus.client.api.AsyncTask;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.ResourceProvider;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.base.TimeUnit;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.util.LocalContext;

@Service
public class HelloWorldService implements MessageCallback {
    public void callback(Message message) {
        LocalContext ctx = LocalContext.get(message);

        AsyncTask task = ctx.getAttribute(AsyncTask.class);
        if (task == null) {

            task = MessageBuilder.createConversation(message)
                    .subjectProvided()
                    .signalling()
                    .withProvided("Data", new ResourceProvider() {
                        public Object get() {
                            return System.currentTimeMillis() + "";
                        }
                    }).noErrorHandling().replyRepeating(TimeUnit.MILLISECONDS, 50);

            ctx.setAttribute(AsyncTask.class, task);
        } else {
            task.cancel(true);
            ctx.removeAttribute(AsyncTask.class);
        }
    }
}
