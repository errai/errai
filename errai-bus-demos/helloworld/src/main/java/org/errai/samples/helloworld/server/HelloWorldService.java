package org.errai.samples.helloworld.server;

import com.google.inject.Inject;
import org.jboss.errai.bus.client.api.AsyncTask;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.ResourceProvider;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.base.TimeUnit;
import org.jboss.errai.bus.client.framework.RequestDispatcher;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.util.LocalContext;


@Service
public class HelloWorldService implements MessageCallback {

    @Inject
    public RequestDispatcher dispatcher;

    public void callback(Message message) {

        LocalContext ctx = LocalContext.get(message);

        AsyncTask task = ctx.getAttribute(AsyncTask.class);
        if (task != null) {
            task.cancel(false);
            ctx.removeAttribute(AsyncTask.class);
        } else {

            task = MessageBuilder.createConversation(message)
                    .toSubject("DataThing")
                    .withProvided("Data", new ResourceProvider<String>() {
                        public String get() {
                            return System.currentTimeMillis() + "";
                        }
                    })
                    .noErrorHandling().sendRepeatingWith(dispatcher, TimeUnit.MILLISECONDS, 50);

            ctx.setAttribute(AsyncTask.class, task);

        }
    }
}
