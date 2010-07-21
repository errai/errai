/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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

import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.widgets.client.WSGrid;


public class WSCellWidgetCell extends WSCellFormatter<Widget> {

    Widget widget = null;

    public WSCellWidgetCell(Widget widget) {
        this.widget = widget;
        this.readonly = false;
    }


    public boolean edit(WSGrid.WSCell element) {
        return false;
    }

    public void stopedit() {
        // do nothing
    }

    public String getTextValue() {
        return widget.toString();
    }

    public Widget getWidget(WSGrid grid) {
        return widget;
    }

    public void setHeight(String height) {
        widget.setHeight(height);
    }

    public void setWidth(String width) {
        widget.setWidth(width);
    }

    @Override
    public Widget getValue() {
        return widget;
    }
}
