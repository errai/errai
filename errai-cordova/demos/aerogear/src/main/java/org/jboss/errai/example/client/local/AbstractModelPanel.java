package org.jboss.errai.example.client.local;

import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.aerogear.api.pipeline.Pipe;
import org.jboss.errai.example.client.local.pipe.TaskPipe;
import org.jboss.errai.example.shared.Task;

import javax.inject.Inject;

/**
 * @author edewit@redhat.com
 */
public abstract class AbstractModelPanel extends Composite {

  @Inject
  @TaskPipe
  protected Pipe<Task> taskPipe;

  protected native void show(com.google.gwt.dom.client.Element element) /*-{
      var target = $wnd.$(element);
      target.slideUp('slow');
      target.next().slideDown('slow');
  }-*/;

  protected native void hide(com.google.gwt.dom.client.Element element) /*-{
      var target = $wnd.$(element);
      target.slideDown('slow');
      target.next().slideUp('slow');
  }-*/;

}
