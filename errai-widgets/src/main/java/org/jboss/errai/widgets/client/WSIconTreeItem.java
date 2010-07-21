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

package org.jboss.errai.widgets.client;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TreeItem;

public class WSIconTreeItem {
    public static TreeItem create(Image icon, final String name) {
        TreeItem item;
        
        if (icon != null) {
            item = new TreeItem("<span unselectable=\"on\"><img src=\""
                    + icon.getUrl() + "\" height=\"16\" width=\"16\" align=\"left\"/>"
                    + name + "</span>");
        }
        else {
            item = new TreeItem("<span unselectable=\"on\">" + name + "</span");
        }

        return item;
    }
}