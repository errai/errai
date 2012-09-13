package org.jboss.errai.demo.jpa.client.local;

import java.util.List;

import org.jboss.errai.demo.jpa.client.shared.Album;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.i18n.shared.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;

public class AlbumTable extends FlexTable {

  private RowOperationHandler<Album> deleteHandler;

  public AlbumTable() {
    addStyleName("albumTable");

    ColumnFormatter cf = getColumnFormatter();
    cf.setStyleName(0, "albumName");
    cf.setStyleName(1, "artistName");
    cf.setStyleName(2, "format");
    cf.setStyleName(3, "releaseDate");
    cf.setStyleName(4, "deleteButton");
  }

  public void addAll(List<Album> entries) {
    for (Album a : entries) {
      add(a);
    }
  }

  public void add(final Album a) {

    Button deleteButton = new Button("Delete");
    deleteButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (deleteHandler != null) {
          deleteHandler.handle(a);
        }
      }
    });

    int row = getRowCount();
    insertRow(row);
    insertCells(row, 0, 5);
    setText(row, 0, a.getName());
    setText(row, 1, a.getArtist().getName());
    setText(row, 2, a.getFormat().name());
    setText(row, 3, DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT).format(a.getReleaseDate()));
    setWidget(row, 5, deleteButton);
  }

  public void setDeleteHandler(RowOperationHandler<Album> handler) {
    deleteHandler = handler;
  }
}
