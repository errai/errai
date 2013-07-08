package org.jboss.errai.example.client.local.pipe;

import org.jboss.errai.aerogear.api.datamanager.DataManager;
import org.jboss.errai.aerogear.api.datamanager.Store;
import org.jboss.errai.aerogear.api.datamanager.StoreType;
import org.jboss.errai.example.shared.Tag;

import javax.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author edewit@redhat.com
 */
@ApplicationScoped
public class TagStore implements Store<Tag> {

  private Store<Tag> tagStore;

  public TagStore() {
    tagStore = new DataManager().store(Tag.class, "tagStore");
  }

  public StoreType getType() {
    return tagStore.getType();
  }

  public Collection<Tag> readAll() {
    return tagStore.readAll();
  }

  public Tag read(Serializable id) {
    return tagStore.read(id);
  }

  public List<Tag> readAll(List<Serializable> ids) {
    List<Tag> result = new ArrayList<Tag>(ids.size());
    for (Serializable serializable : ids) {
      result.add(tagStore.read(serializable));
    }
    return result;
  }

  public void save(Tag item) {
    tagStore.save(item);
  }

  public void saveAll(List<Tag> TagList) {
    for (Tag Tag : TagList) {
      save(Tag);
    }
  }

  public void saveAllTags(List<Tag> tagList) {
    for (Tag tag : tagList) {
      tagStore.save(tag);
    }
  }

  public void reset() {
    tagStore.reset();
  }

  public void remove(Serializable id) {
    tagStore.remove(id);
  }
}
