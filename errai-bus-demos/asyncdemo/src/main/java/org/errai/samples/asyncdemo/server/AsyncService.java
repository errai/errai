package org.errai.samples.asyncdemo.server;

import org.jboss.errai.bus.client.api.AsyncTask;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.ResourceProvider;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.base.TimeUnit;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.util.LocalContext;


@Service
public class AsyncService implements MessageCallback {
    public void callback(Message message) {
        LocalContext ctx = LocalContext.get(message);

        AsyncTask task = ctx.getAttribute(AsyncTask.class);

        if ("Start".equals(message.getCommandType())) {
            // there's no task running in this context.
            if (task == null) {
                ResourceProvider<Double> randomNumberProvider = new ResourceProvider<Double>() {
                    public Double get() {
                        return Math.random();
                    }
                };
                
                task = MessageBuilder.createConversation(message)
                        .subjectProvided().signalling()
                        .withProvided("Data", randomNumberProvider)
                        .noErrorHandling()
                        .replyRepeating(TimeUnit.MILLISECONDS, 100);

                ctx.setAttribute(AsyncTask.class, task);
            }
            else {
                System.out.println("Task already started!");
            }
        }
        else if ("Stop".equals(message.getCommandType())) {
            if (task == null) {
                System.out.println("Nothing to stop!");
            }
            else {
                task.cancel(true);
                ctx.removeAttribute(AsyncTask.class);
            }
        }
    }
}
