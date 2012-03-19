package org.jboss.errai.jpa.test.entity;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

//TODO annotate with @TestOnly after merging in the fix from master branch
@Entity
public class Artist {

  private String name;

  @OneToMany(mappedBy="artist")
  private Set<Album> albums;

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
}
