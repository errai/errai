package org.jboss.errai.demo.jpa.client.local;

import javax.persistence.EntityManager;

import org.jboss.errai.demo.jpa.client.shared.Album;
import org.jboss.errai.demo.jpa.client.shared.Artist;
import org.jboss.errai.demo.jpa.client.shared.Format;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.datepicker.client.DatePicker;

/**
 * A form for editing an instance of Album.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class AlbumForm extends Composite {

  // Note: for simplicity, and to keep this JPA demo on-topic, we are using
  // plain GWT programmatic layout rather than ErraiUI, UIBinder, or Errai Data
  // Binding. See the grocery list demo for the full-on Errai development experience.

  private final EntityManager em;

  private final Album album;

  private final TextBox name = new TextBox();
  private final ListBox artist = new ListBox();
  private final ListBox format = new ListBox();
  private final DatePicker releaseDate = new DatePickerWithYearSelector();

  private final Button saveButton = new Button("Save");

  private RowOperationHandler<Album> saveHandler;

  public AlbumForm(final Album album, EntityManager em) {
    this.album = album;
    this.em = em;

    for (Artist a : em.createNamedQuery("allArtistsByName", Artist.class).getResultList()) {
      artist.addItem(a.getName(), String.valueOf(a.getId()));
    }

    for (Format f : Format.values()) {
      format.addItem(f.name());
    }

    saveButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        updateAlbumFromUI();
        if (saveHandler != null) {
          saveHandler.handle(album);
        }
      }
    });

    updateUIFromAlbum();

    final Grid g = new Grid(5, 2);

    int row = 0;
    g.setText(row, 0, "Name:");
    g.setWidget(row, 1, name);

    row++;
    g.setText(row, 0, "Artist:");
    g.setWidget(row, 1, artist);

    row++;
    g.setText(row, 0, "Format:");
    g.setWidget(row, 1, format);

    row++;
    g.setText(row, 0, "Release Date:");
    g.setWidget(row, 1, releaseDate);

    row++;
    g.setWidget(row, 1, saveButton);

    initWidget(g);
  }

  protected void updateAlbumFromUI() {
    album.setName(name.getText());
    long artistId = Long.parseLong(artist.getValue(artist.getSelectedIndex()));
    album.setArtist(em.find(Artist.class, artistId));
    album.setFormat(Format.valueOf(format.getValue(format.getSelectedIndex())));
    album.setReleaseDate(releaseDate.getValue());
  }

  private void updateUIFromAlbum() {

    // Note: with Errai Data Sync, the code in this method would be unnecessary. See the Grocery List demo for an example.

    name.setText(album.getName());

    if (album.getArtist() != null) {
      for (int i = 0; i < artist.getItemCount(); i++) {
        if (Long.parseLong(artist.getValue(i)) == album.getArtist().getId()) {
          artist.setSelectedIndex(i);
        }
      }
    }

    if (album.getFormat() != null) {
      format.setSelectedIndex(album.getFormat().ordinal());
    }
    else {
      format.setSelectedIndex(Format.LP.ordinal());
    }

    if (album.getReleaseDate() != null) {
      releaseDate.setValue(album.getReleaseDate());
      releaseDate.setCurrentMonth(album.getReleaseDate());
    }
  }

  public void setSaveHandler(RowOperationHandler<Album> handler) {
    this.saveHandler = handler;
  }

  public void grabFocus() {
    name.setFocus(true);
  }
}
