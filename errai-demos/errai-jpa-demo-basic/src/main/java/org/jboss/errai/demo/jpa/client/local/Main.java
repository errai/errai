/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;

@EntryPoint
public class Main {

    @Inject
    EntityManager em;

    private AlbumTable albumsWidget = new AlbumTable();

    private Button resetEverythingButton = new Button("Reset all data to defaults");
    private Button newAlbumButton = new Button("New Album...");
    private Button newArtistButton = new Button("New Artist...");

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

        albumsWidget.setEditHandler(new RowOperationHandler<Album>() {
            @Override
            public void handle(Album a) {
                AlbumForm af = new AlbumForm(a, em);
                final PopupPanel pp = new PopupPanel(true, true);
                af.setSaveHandler(new RowOperationHandler<Album>() {
                    @Override
                    public void handle(Album album) {
                        em.flush();
                        refreshUI();
                        pp.hide();
                    }
                });
                pp.setWidget(af);
                pp.setGlassEnabled(true);
                pp.show();
                af.grabFocus();
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

        newAlbumButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                AlbumForm af = new AlbumForm(new Album(), em);
                final PopupPanel pp = new PopupPanel(true, true);
                af.setSaveHandler(new RowOperationHandler<Album>() {
                    @Override
                    public void handle(Album album) {
                        em.persist(album);
                        em.flush();
                        refreshUI();
                        pp.hide();
                    }
                });
                pp.setWidget(af);
                pp.setGlassEnabled(true);
                pp.show();
                af.grabFocus();
            }
        });

        newArtistButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ArtistForm af = new ArtistForm(new Artist(), em);
                final PopupPanel pp = new PopupPanel(true, true);
                af.setSaveHandler(new RowOperationHandler<Artist>() {
                    @Override
                    public void handle(Artist artist) {
                        em.persist(artist);
                        em.flush();
                        refreshUI();
                        pp.hide();
                    }
                });
                pp.setWidget(af);
                pp.setGlassEnabled(true);
                pp.show();
                af.grabFocus();
            }
        });

        preFillDatabaseIfEmpty();

        refreshUI();

        RootPanel.get().add(resetEverythingButton);
        RootPanel.get().add(albumsWidget);
        RootPanel.get().add(newAlbumButton);
        RootPanel.get().add(newArtistButton);
    }

    private void refreshUI() {
        TypedQuery<Album> albums = em.createNamedQuery("allAlbums", Album.class);
        albumsWidget.removeAllRows();
        albumsWidget.addAll(albums.getResultList());
    }

    /**
     * If there are no Album instances in the database, this method creates and persists a selection of music from the 1960's.
     */
    private void preFillDatabaseIfEmpty() {
        TypedQuery<Album> albums = em.createNamedQuery("allAlbums", Album.class);
        if (albums.getResultList().isEmpty()) {
            Genre rock = new Genre("Rock");
            Genre soul = new Genre("Soul");
            Genre rnb = new Genre("R&B");

            Artist beatles = new Artist();
            beatles.setName("The Beatles");
            beatles.addGenre(rock);

            Artist samNDave = new Artist();
            samNDave.setName("Sam & Dave");
            samNDave.addGenre(rock);
            samNDave.addGenre(soul);
            samNDave.addGenre(rnb);

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

            album = new Album();
            album.setArtist(beatles);
            album.setFormat(Format.LP);
            album.setName("Magical Mystery Tour");
            album.setReleaseDate(new Date(-66164400000L));
            em.persist(album);

            album = new Album();
            album.setArtist(beatles);
            album.setFormat(Format.LP);
            album.setName("Sgt. Pepper's Lonely Hearts Club Band");
            album.setReleaseDate(new Date(-81633600000L));
            em.persist(album);

            album = new Album();
            album.setArtist(beatles);
            album.setFormat(Format.LP);
            album.setName("Revolver");
            album.setReleaseDate(new Date(-107553600000L));
            em.persist(album);

            album = new Album();
            album.setArtist(beatles);
            album.setFormat(Format.LP);
            album.setName("Rubber Soul");
            album.setReleaseDate(new Date(-128718000000L));
            em.persist(album);

            album = new Album();
            album.setArtist(samNDave);
            album.setFormat(Format.LP);
            album.setName("Hold On, I'm Comin'");
            album.setReleaseDate(new Date(-121114800000L));
            em.persist(album);

            album = new Album();
            album.setArtist(samNDave);
            album.setFormat(Format.LP);
            album.setName("Double Dynamite");
            album.setReleaseDate(new Date(-97354800000L));
            em.persist(album);

            album = new Album();
            album.setArtist(samNDave);
            album.setFormat(Format.LP);
            album.setName("Soul Men");
            album.setReleaseDate(new Date(-71092800000L));
            em.persist(album);

            // Some extra genres to play with
            em.persist(new Genre("Classical"));
            em.persist(new Genre("Country"));
            em.persist(new Genre("Folk"));
            em.persist(new Genre("Funk"));
            em.persist(new Genre("Pop"));

            // store them
            em.flush();
        }
    }
}
