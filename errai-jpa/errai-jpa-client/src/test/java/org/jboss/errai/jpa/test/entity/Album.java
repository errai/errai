package org.jboss.errai.jpa.test.entity;

import java.sql.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

//TODO annotate with @TestOnly after merging in the fix from master branch
@Entity
public class Album {

  @GeneratedValue
  @Id
  private long id;

  private String name;

  @ManyToOne
  private Artist artist;

  private Date releaseDate;

  public long getId() {
    return id;
  }

  public void setId(long id) {
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
}
