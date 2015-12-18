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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.jboss.errai.demo.jpa.client.shared.Artist;
import org.jboss.errai.demo.jpa.client.shared.Genre;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

/**
 * A form for editing an instance of Artist.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class ArtistForm extends Composite {

    // Note: for simplicity, and to keep this JPA demo on-topic, we are using
    // plain GWT programmatic layout rather than ErraiUI, UIBinder, or Errai Data
    // Binding. See the grocery list demo for the full-on Errai development experience.

    private final EntityManager em;

    private final Artist artist;

    private final TextBox name = new TextBox();
    private final ListBox genres = new ListBox(true);

    private final Button saveButton = new Button("Save");

    private RowOperationHandler<Artist> saveHandler;

    public ArtistForm(final Artist artist, EntityManager em) {
        this.artist = artist;
        this.em = em;

        for (Genre g : em.createNamedQuery("allGenresByName", Genre.class).getResultList()) {
            genres.addItem(g.getName(), String.valueOf(g.getId()));
        }

        saveButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                updateArtistFromUI();
                if (saveHandler != null) {
                    saveHandler.handle(artist);
                }
            }
        });

        updateUIFromArtist();

        Grid g = new Grid(3, 2);

        int row = 0;
        g.setText(row, 0, "Name:");
        g.setWidget(row, 1, name);

        row++;
        g.setText(row, 0, "Genres:");
        g.setWidget(row, 1, genres);

        row++;
        g.setWidget(row, 1, saveButton);

        initWidget(g);
    }

    protected void updateArtistFromUI() {
        artist.setName(name.getText());

        Set<Integer> selectedGenreIds = new HashSet<Integer>();
        for (int i = 0; i < genres.getItemCount(); i++) {
            if (genres.isItemSelected(i)) {
                selectedGenreIds.add(Integer.valueOf(genres.getValue(i)));
            }
        }
        TypedQuery<Genre> q = em.createNamedQuery("genresWithId", Genre.class);
        q.setParameter("idSet", selectedGenreIds);
        artist.setGenres(new HashSet<Genre>(q.getResultList()));
    }

    private void updateUIFromArtist() {

        // Note: with Errai Data Sync, the code in this method would be unnecessary. See the Grocery List demo for an example.

        name.setText(artist.getName());

        Set<Integer> genresToSelect = new HashSet<Integer>();
        for (Genre g : artist.getGenres()) {
            genresToSelect.add(g.getId());
        }

        if (artist.getGenres() != null) {
            for (int i = 0; i < genres.getItemCount(); i++) {
                genres.setItemSelected(i, genresToSelect.contains(Integer.parseInt(genres.getValue(i))));
            }
        }
    }

    public void setSaveHandler(RowOperationHandler<Artist> handler) {
        this.saveHandler = handler;
    }

    public void grabFocus() {
        name.setFocus(true);
    }
}
