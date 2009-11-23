/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.widgets.client.listeners;

import com.google.gwt.event.dom.client.ChangeEvent;
import org.jboss.errai.widgets.client.WSGrid;
import org.jboss.errai.widgets.client.format.WSCellFormatter;

public class CellChangeEvent extends ChangeEvent {
    private WSGrid.WSCell cell;
    private Object newValue;

    public CellChangeEvent(WSGrid.WSCell cell, Object newValue) {
        this.cell = cell;
        this.newValue = newValue;
    }

    public WSGrid.WSCell getCell() {
        return cell;
    }

    public Object getNewValue() {
        return newValue;
    }

    public Object getOldValue() {
        return cell.getCellFormat().getValue();
    }
}
