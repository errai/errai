package org.jboss.errai.demo.jpa.client.local;

import java.sql.Date;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.jboss.errai.demo.jpa.client.shared.Album;
import org.jboss.errai.demo.jpa.client.shared.Artist;
import org.jboss.errai.demo.jpa.client.shared.Format;
import org.jboss.errai.demo.jpa.client.shared.Genre;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.jpa.client.local.ErraiEntityManager;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;

@EntryPoint
public class Main {

  @Inject EntityManager em;

  private AlbumTable albumsWidget = new AlbumTable();

  private Button resetEverythingButton = new Button("Reset all data to defaults");

  @PostConstruct
  public void init() {

    albumsWidget.setDeleteHandler(new RowOperationHandler<Album>() {
      @Override
      public void handle(Album a) {
        em.remove(a);
        em.flush();
        refreshUI();
      }
    });

    resetEverythingButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        ((ErraiEntityManager) em).removeAll();
        preFillDatabaseIfEmpty();
        refreshUI();
      }
    });

    preFillDatabaseIfEmpty();
    refreshUI();

    RootPanel.get().add(albumsWidget);
    RootPanel.get().add(resetEverythingButton);
  }

  private void refreshUI() {
    TypedQuery<Album> albums = em.createNamedQuery("allAlbums", Album.class);
    albumsWidget.removeAllRows();
    albumsWidget.addAll(albums.getResultList());
  }

  private void preFillDatabaseIfEmpty() {
    TypedQuery<Album> albums = em.createNamedQuery("allAlbums", Album.class);
    if (albums.getResultList().isEmpty()) {
      Artist beatles = new Artist();
      beatles.setName("The Beatles");
      beatles.addGenre(new Genre("Rock"));

      Album album = new Album();
      album.setArtist(beatles);
      album.setFormat(Format.LP);
      album.setName("Let It Be");
      album.setReleaseDate(new Date(11012400000L));
      em.persist(album);

      album = new Album();
      album.setArtist(beatles);
      album.setFormat(Format.LP);
      album.setName("Abbey Road");
      album.setReleaseDate(new Date(-8366400000L));
      em.persist(album);

      album = new Album();
      album.setArtist(beatles);
      album.setFormat(Format.LP);
      album.setName("Yellow Submarine");
      album.setReleaseDate(new Date(-30481200000L));
      em.persist(album);

      album = new Album();
      album.setArtist(beatles);
      album.setFormat(Format.LP);
      album.setName("The Beatles");
      album.setReleaseDate(new Date(-34974000000L));
      em.persist(album);

      // store them
      em.flush();
    }
  }

}
