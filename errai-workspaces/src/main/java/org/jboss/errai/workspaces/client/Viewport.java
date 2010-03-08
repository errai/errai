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
