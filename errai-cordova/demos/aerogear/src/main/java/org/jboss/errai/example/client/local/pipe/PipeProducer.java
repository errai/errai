package org.jboss.errai.example.client.local.pipe;

import org.jboss.errai.aerogear.api.pipeline.Pipe;
import org.jboss.errai.aerogear.api.pipeline.PipeFactory;
import org.jboss.errai.example.shared.Project;
import org.jboss.errai.example.shared.Tag;
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
  @Tasks
  private Pipe<Task> createTaskPipe() {
    return createPipe(Task.class, new Config("tasks"));
  }

  @Produces
  @Projects
  private Pipe<Project> createProjectPipe() {
    return createPipe(Project.class, new Config("projects"));
  }

  @Produces
  @Tags
  private Pipe<Tag> createTagPipe() {
    return createPipe(Tag.class, new Config("tags"));
  }

  private <T> Pipe<T> createPipe(Class<T> type, Config config) {
    return new PipeFactory().createPipe(type, config);
  }
}
