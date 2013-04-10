package org.jboss.errai.example.client.local.pipe;

import org.jboss.errai.aerogear.api.datamanager.DataManager;
import org.jboss.errai.aerogear.api.datamanager.Store;
import org.jboss.errai.example.shared.Project;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

/**
 * @author edewit@redhat.com
 */
@ApplicationScoped
public class StoreProducer {

  @ApplicationScoped
  @Produces
  private Store<Project> projectStore() {
    return new DataManager().store(Project.class, "projectStore");
  }
}
