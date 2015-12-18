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

import java.util.List;

import org.jboss.errai.demo.jpa.client.shared.Album;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.i18n.shared.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;

/**
 * A tabular display of a list of albums, supporting delete and edit callbacks for each row.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class AlbumTable extends FlexTable {

    private RowOperationHandler<Album> deleteHandler;
    private RowOperationHandler<Album> editHandler;

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

        Button editButton = new Button("Edit...");
        editButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (editHandler != null) {
                    editHandler.handle(a);
                }
            }
        });

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
        setText(row, 1, a.getArtist() == null ? "" : a.getArtist().getName());
        setText(row, 2, a.getFormat() == null ? "" : a.getFormat().name());
        setText(row, 3, a.getReleaseDate() == null ? "" :
            DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT).format(a.getReleaseDate()));
        setWidget(row, 5, editButton);
        setWidget(row, 6, deleteButton);
    }

    public void setDeleteHandler(RowOperationHandler<Album> handler) {
        deleteHandler = handler;
    }

    public void setEditHandler(RowOperationHandler<Album> handler) {
        editHandler = handler;
    }
}
