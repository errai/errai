package org.jboss.errai.example.client.local.pipe;

import org.jboss.errai.aerogear.api.pipeline.Pipe;
import org.jboss.errai.aerogear.api.pipeline.PipeFactory;
import org.jboss.errai.example.shared.Project;
import org.jboss.errai.example.shared.Task;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import static org.jboss.errai.aerogear.api.pipeline.PipeFactory.Config;

/**
 * @author edewit@redhat.com
 */
@ApplicationScoped
public class PipeProducer {

  @Produces
  @TaskPipe
  private Pipe<Task> createTaskPipe() {
    return createPipe(Task.class, new Config("tasks"));
  }

  @Produces
  @ProjectPipe
  private Pipe<Project> createProjectPipe() {
    return createPipe(Project.class, new Config("projects"));
  }

  private <T> Pipe<T> createPipe(Class<T> type, Config config) {
    return new PipeFactory().createPipe(type, config);
  }
}
