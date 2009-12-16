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

package org.jboss.errai.widgets.rebind.widgetmappers;

import com.google.gwt.user.client.ui.CheckBox;
import org.jboss.errai.widgets.rebind.widgetmappers.col.CheckBoxColMapper;

import java.util.HashMap;
import java.util.Map;

public class ColMappers {
    private static final Map<String, ColMapper> COLMAPPERS = new HashMap<String, ColMapper>();
    static {
        addMapper(CheckBox.class, new CheckBoxColMapper());
    }

    public static boolean hasMapper(Class mapperType) {
        return hasMapper(mapperType.toString());
    }

    public static boolean hasMapper(String mapperType) {
        return COLMAPPERS.containsKey(mapperType);
    }

    public static ColMapper getMapper(Class mapperType) {
        return getMapper(mapperType.toString());
    }

    public static ColMapper getMapper(String mapperType) {
        return COLMAPPERS.get(mapperType);
    }

    public static void addMapper(Class mapperType, ColMapper mapper) {
        addMapper(mapperType.getName(), mapper);
    }

    public static void addMapper(String mapperType, ColMapper mapper) {
        COLMAPPERS.put(mapperType, mapper);
    }
}
