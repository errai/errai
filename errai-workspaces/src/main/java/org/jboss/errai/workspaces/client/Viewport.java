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
package org.jboss.errai.workspaces.client;

import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Timer;
import org.jboss.errai.workspaces.client.util.LayoutUtil;

public class Viewport extends org.gwt.mosaic.ui.client.Viewport
{
  
  public void onResize(ResizeEvent event) {
    super.onResize(event);
    DeferredCommand.addCommand(
        new Command() {
          public void execute()
          {
            /**
             * Mosaic seems to be doing something weird in it's layout calculations right now that
             * I can't trace down, so my only solution is to create a timer-based delay...
             *
             * This is really sloppy... and contributes to what is already a very slow resizing
             * experience with the Mosaic layouts...
             */

            Timer layoutHintDelay = new Timer() {

              public void run() {
                LayoutUtil.layoutHints(getLayoutPanel());
              }
            };

            layoutHintDelay.schedule(500);
          }
        });
  }
}
