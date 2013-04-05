package org.jboss.errai.example.client.local.pipe;

import org.jboss.errai.aerogear.api.pipeline.Pipe;
import org.jboss.errai.aerogear.api.pipeline.PipeFactory;
import org.jboss.errai.example.shared.Task;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

/**
 * @author edewit@redhat.com
 */
@ApplicationScoped
public class PipeProducer {

  @Produces
  @TaskPipe
  private Pipe<Task> createPipe() {
    PipeFactory.Config config = new PipeFactory.Config("tasks");
    final Pipe<Task> pipe = new PipeFactory().createPipe(Task.class, config);
    return pipe;
  }
}
