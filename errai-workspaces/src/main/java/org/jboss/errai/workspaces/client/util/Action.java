/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.errai.workspaces.client.util;

import com.google.gwt.user.client.Command;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.Message;
import org.jboss.errai.bus.client.MessageCallback;

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
