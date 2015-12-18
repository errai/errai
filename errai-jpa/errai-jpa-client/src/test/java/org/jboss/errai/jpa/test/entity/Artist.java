package org.jboss.errai.jpa.test.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
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

  // a two-way relationship (albums refer back to artists)
  @OneToMany(mappedBy="artist", cascade=CascadeType.ALL)
  private Set<Album> albums = new HashSet<Album>();

  // a one-way relationship (genres don't reference artists)
  @OneToMany(cascade={CascadeType.PERSIST, CascadeType.MERGE})
  private Set<Genre> genres = new HashSet<Genre>();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Set<Album> getAlbums() {
    return albums;
  }

  public void setAlbums(Set<Album> albums) {
    this.albums = albums;
  }

  public void addAlbum(Album album) {
    albums.add(album);
  }

  public Set<Genre> getGenres() {
    return genres;
  }

  public void setGenres(Set<Genre> genres) {
    this.genres = genres;
  }

  public void addGenre(Genre genre) {
    genres.add(genre);
  }

  @Override
  public String toString() {
    // BEWARE: the tests depend on this toString() to fully represent the state of the class
    return "Artist [id=" + id + ", name=" + name + ", albums=" + albums + ", genres=" + genres + "]";
  }
}
