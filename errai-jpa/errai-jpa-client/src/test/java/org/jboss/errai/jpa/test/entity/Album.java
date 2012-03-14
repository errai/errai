package org.jboss.errai.jpa.test.entity;

import java.sql.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.jboss.errai.ioc.client.api.TestOnly;

@TestOnly @Entity
public class Album {

  private String name;

  @ManyToOne
  private Artist artist;

  private Date releaseDate;

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
}
