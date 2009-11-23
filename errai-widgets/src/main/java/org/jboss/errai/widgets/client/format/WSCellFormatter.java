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

package org.jboss.errai.widgets.client.format;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.widgets.client.WSGrid;

//todo: this totally needs to be refactored... the formatter currently holds the value...
public abstract class WSCellFormatter<T> {
    protected static WSGrid.WSCell wsCellReference;
    protected HTML html;
    protected boolean readonly = false;
    protected boolean cancel = false;

    public void cancelEdit() {
        cancel = true;
    }

    public void setValue(T value) {
        try {
            if (readonly) return;

            String str = String.valueOf(value);

            notifyCellUpdate(str);

            if (!cancel) {
                if (value == null || str.length() == 0) {
                    html.setHTML("&nbsp;");
                    return;
                }

                html.setHTML(str);
            } else
                cancel = false;

        }
        finally {
            notifyCellAfterUpdate();
        }
    }

    public String getTextValue() {
        return html.getHTML().equals("&nbsp;") ? "" : html.getHTML();
    }

    public abstract T getValue();

    public Widget getWidget(WSGrid grid) {
        return html;
    }

    public abstract boolean edit(WSGrid.WSCell element);

    public abstract void stopedit();

    public void setHeight(String height) {
        html.setHeight(height);
    }

    public void setWidth(String width) {
        html.setWidth(width);
    }


    /**
     * Notify any registered listeners that the value is about to change.
     *
     * @param newValue
     */
    public void notifyCellUpdate(Object newValue) {
        if (wsCellReference == null) return;
        wsCellReference.notifyCellUpdate(newValue);
    }

    public void notifyCellAfterUpdate() {
        if (wsCellReference == null) return;
        wsCellReference.notifyCellAfterUpdate();
    }
}
