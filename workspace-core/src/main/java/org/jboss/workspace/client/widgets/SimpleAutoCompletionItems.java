/*
Auto-Completion Textbox for GWT
Copyright (C) 2006 Oliver Albers http://gwt.components.googlepages.com/

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
*/
package org.jboss.workspace.client.widgets;

import java.util.ArrayList;

public class SimpleAutoCompletionItems implements CompletionItems {
    private String[] completions;

    public SimpleAutoCompletionItems(String[] items) {
        completions = items;
    }

    public String[] getCompletionItems(String match) {
        ArrayList matches = new ArrayList();
        for (int i = 0; i < completions.length; i++) {
            if (completions[i].toLowerCase().startsWith(match.toLowerCase())) {
                matches.add(completions[i]);
            }
        }
        String[] returnMatches = new String[matches.size()];
        for (int i = 0; i < matches.size(); i++) {
            returnMatches[i] = (String) matches.get(i);
        }
        return returnMatches;
    }
}

