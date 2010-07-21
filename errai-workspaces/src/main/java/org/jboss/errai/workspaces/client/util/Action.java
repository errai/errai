
/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.workspaces.client.util;

import com.google.gwt.user.client.Command;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;

/**
 * Utility builder pattern to improve readbility
 * of listener based code snippets
 */
public class Action
{
  private Command task;
  private String subject = null;

  private Action(Command task)
  {
    this.task = task;
  }

  public static Action perform(Command task)
  {
    return new Action(task);
  }

  public Action as(String subject)
  {
    this.subject = subject;
    return this;
  }

  public void on(final Enum command)
  {
    if(null==subject)
      throw new IllegalStateException("Subject not set, Make sure to call as(...) beforehand");
    
    ErraiBus.get().subscribe(subject, new MessageCallback()
    {
      public void callback(Message message)
      {
        if(message.getCommandType().equals(command.toString()))
          task.execute();
      }
    });

  }
}
