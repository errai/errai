/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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

