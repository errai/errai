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
