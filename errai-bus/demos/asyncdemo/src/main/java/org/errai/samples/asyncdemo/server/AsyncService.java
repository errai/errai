/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.errai.samples.asyncdemo.server;

import java.util.regex.Pattern;

import org.jboss.errai.bus.client.api.AsyncTask;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.common.client.api.ResourceProvider;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.base.TimeUnit;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.util.LocalContext;


@Service
public class AsyncService implements MessageCallback {

  private final static Pattern StartMatcher = Pattern.compile("^Start[0-9]{1}$");
  private final static Pattern StopMatcher = Pattern.compile("^Stop[0-9]{1}$");

  public void callback(Message message) {
    LocalContext ctx = LocalContext.get(message);

    String commandType = message.getCommandType();
    String taskName = getTaskName(commandType);

    if (StartMatcher.matcher(commandType).matches()) {
      AsyncTask task = ctx.getAttribute(AsyncTask.class, taskName);

      // there's no task running in this context.
      if (task == null) {
        ResourceProvider<Double> randomNumberProvider = new ResourceProvider<Double>() {
          public Double get() {
            return Math.random();
          }
        };

        task = MessageBuilder.createConversation(message)
            .subjectProvided()
            .withProvided("Data", randomNumberProvider)
            .noErrorHandling()
            .replyRepeating(TimeUnit.MILLISECONDS, 50);

        System.out.println("New task started: " + taskName);
        ctx.setAttribute(taskName, task);
      }
      else {
        System.out.println("Task already started: " + taskName);
      }
    }
    else if (StopMatcher.matcher(commandType).matches()) {
      AsyncTask task = ctx.getAttribute(AsyncTask.class, taskName);

      if (task == null) {
        System.out.println("Nothing to stop: " + taskName);
      }
      else {
        System.out.println("Stopping: " + taskName);
        task.cancel(true);
        ctx.removeAttribute(taskName);
      }
    }
  }

  public String getTaskName(String name) {
    return "Task" + getLastChar(name);
  }

  public char getLastChar(String str) {
    return str.charAt(str.length() - 1);
  }


}
