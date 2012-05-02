package org.jboss.errai.jpa.test.entity;

import java.sql.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.ioc.client.api.TestOnly;

@TestOnly @Portable @Entity
public class Album {

  @GeneratedValue
  @Id
  private Long id;

  private String name;

  @ManyToOne
  private Artist artist;

  private Date releaseDate;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public Artist getArtist() {
    return artist;
  }

  public Date getReleaseDate() {
    return releaseDate;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setArtist(Artist artist) {
    this.artist = artist;
  }

  public void setReleaseDate(Date releaseDate) {
    this.releaseDate = releaseDate;
  }

  @Override
  public String toString() {
    // BEWARE: the tests depend on this toString() to fully represent the state of the class
    // BEWARE2: don't cascade the toString() to artist, or you will create infinite recursion
    return "Album [id=" + id + ", name=" + name + ", artist="
            + (artist == null ? "null" : artist.getName())
            + ", releaseDate=" + releaseDate + "]";
  }
}
