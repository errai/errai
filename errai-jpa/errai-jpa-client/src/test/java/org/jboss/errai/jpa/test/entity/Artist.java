package org.jboss.errai.jpa.test.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.ioc.client.api.TestOnly;

@TestOnly @Portable @Entity
public class Artist {

  @Id
  private Long id;

  private String name;

  @OneToMany(mappedBy="artist")
  private Set<Album> albums = new HashSet<Album>();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public Set<Album> getAlbums() {
    return albums;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setAlbums(Set<Album> albums) {
    this.albums = albums;
  }

  @Override
  public String toString() {
    // BEWARE: the tests depend on this toString() to fully represent the state of the class
    return "Artist [id=" + id + ", name=" + name + ", albums=" + albums + "]";
  }
}
