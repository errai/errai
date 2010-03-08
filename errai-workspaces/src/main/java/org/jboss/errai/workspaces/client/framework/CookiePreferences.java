
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

package org.jboss.errai.workspaces.client.framework;

import com.google.gwt.user.client.Cookies;

import java.util.Date;

/**
 * Cookie based workspace preferences
 */
public class CookiePreferences implements Preferences
{
  // Tool that should be launched at startup
  public static final String DEFAULT_TOOL = "workspace.default.tool";

  public boolean has(String key)
  {
    return get(key)!=null;
  }

  public String get(String key)
  {
    return Cookies.getCookie(key);
  }

  public void set(String key, String value)
  {
    Date twoWeeks = new Date(System.currentTimeMillis()+(2*604800*1000));
    Cookies.setCookie(key, value, twoWeeks);
  }

  public void clear(String key)
  {
    Cookies.removeCookie(key);
  }
}

