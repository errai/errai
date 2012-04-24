package org.jboss.errai.jpa.test.entity;

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
  private Set<Album> albums;

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
    return "Artist [id=" + id + ", name=" + name + ", albums=" + albums + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((albums == null) ? 0 : albums.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Artist other = (Artist) obj;
    if (albums == null) {
      if (other.albums != null)
        return false;
    }
    else if (!albums.equals(other.albums))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    }
    else if (!id.equals(other.id))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    }
    else if (!name.equals(other.name))
      return false;
    return true;
  }

}
